package com.sinostock.erp.service;

import com.sinostock.erp.domain.*;
import com.sinostock.erp.dto.ApiDtos.*;
import com.sinostock.erp.exception.ApiException;
import com.sinostock.erp.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;
    private final InventoryLogRepository logs;
    public OrderService(OrderRepository orders, ProductRepository products, UserRepository users,
                        InventoryLogRepository logs) {
        this.orders = orders; this.products = products; this.users = users; this.logs = logs;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request, String username) {
        UserAccount user = users.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Tài khoản không hợp lệ"));
        // Merge duplicate product IDs to avoid stock race and duplicate order lines.
        Map<Long, Integer> merged = new TreeMap<>();
        for (OrderLineRequest line : request.items()) {
            if (line.productId() == null || line.quantity() == null || line.quantity() <= 0)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Sản phẩm hoặc số lượng trong đơn không hợp lệ");
            try { merged.merge(line.productId(), line.quantity(), Math::addExact); }
            catch (ArithmeticException ex) { throw new ApiException(HttpStatus.BAD_REQUEST, "Số lượng đặt hàng vượt quá giới hạn"); }
        }
        SaleOrder order = new SaleOrder("SO-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(), user);
        BigDecimal total = BigDecimal.ZERO;
        for (var entry : merged.entrySet()) {
            Product p = products.findById(entry.getKey())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm mã " + entry.getKey()));
            if (!p.isActive()) throw new ApiException(HttpStatus.CONFLICT, "Sản phẩm " + p.getSku() + " đã ngừng kinh doanh");
            OrderItem item = new OrderItem(p, entry.getValue());
            order.addItem(item);
            total = total.add(item.getLineTotalCny());
        }
        order.setTotalAmountCny(total);
        return toResponse(orders.saveAndFlush(order));
    }

    @Transactional
    public OrderResponse confirm(Long id) {
        SaleOrder order = lockedOrder(id);
        if (order.getStatus() != OrderStatus.PENDING)
            throw new ApiException(HttpStatus.CONFLICT, "Chỉ đơn hàng đang chờ mới có thể xác nhận");
        // Sorted product row locks: all orders lock in consistent order to reduce deadlocks.
        for (OrderItem item : sortedItems(order)) {
            Product p = lockedProduct(item.getProduct().getId());
            if (!p.isActive()) throw new ApiException(HttpStatus.CONFLICT, "Sản phẩm ngừng kinh doanh: " + p.getSku());
            if (p.getStockQuantity() < item.getQuantity())
                throw new ApiException(HttpStatus.CONFLICT, "Không đủ tồn kho cho mã SKU " + p.getSku());
            int newStock = p.getStockQuantity() - item.getQuantity();
            p.changeStock(newStock);
            logs.save(new InventoryLog(p, order, -item.getQuantity(), newStock, "ORDER_CONFIRM"));
        }
        order.confirm();
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        SaleOrder order = lockedOrder(id);
        if (order.getStatus() == OrderStatus.CANCELLED)
            throw new ApiException(HttpStatus.CONFLICT, "Đơn hàng đã bị hủy trước đó");
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            for (OrderItem item : sortedItems(order)) {
                Product p = lockedProduct(item.getProduct().getId());
                int newStock;
                try { newStock = Math.addExact(p.getStockQuantity(), item.getQuantity()); }
                catch (ArithmeticException ex) { throw new ApiException(HttpStatus.CONFLICT, "Số lượng tồn kho vượt giới hạn khi hoàn hàng"); }
                p.changeStock(newStock);
                logs.save(new InventoryLog(p, order, item.getQuantity(), newStock, "ORDER_CANCEL"));
            }
        }
        order.cancel();
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return toResponse(orders.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng")));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    private SaleOrder lockedOrder(Long id) {
        return orders.findByIdForUpdate(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
    }
    private Product lockedProduct(Long id) {
        return products.findByIdForUpdate(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product không tồn tại"));
    }
    private List<OrderItem> sortedItems(SaleOrder order) {
        return order.getItems().stream().sorted(Comparator.comparing(i -> i.getProduct().getId())).toList();
    }
    private OrderResponse toResponse(SaleOrder o) {
        List<OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProduct().getId(), i.getProduct().getSku(), i.getProduct().getNameCn(),
                        i.getQuantity(), i.getUnitPriceCny(), i.getLineTotalCny())).toList();
        return new OrderResponse(o.getId(), o.getOrderNumber(), o.getStatus(), o.getTotalAmountCny(),
                o.getCreatedBy().getUsername(), o.getCreatedAt(), o.getConfirmedAt(), items);
    }
}
