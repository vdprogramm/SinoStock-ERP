package com.sinostock.erp.service;

import com.sinostock.erp.domain.OrderStatus;
import com.sinostock.erp.domain.SaleOrder;
import com.sinostock.erp.dto.ApiDtos.*;
import com.sinostock.erp.repository.OrderRepository;
import com.sinostock.erp.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private final ProductRepository products;
    private final OrderRepository orders;
    public DashboardService(ProductRepository products, OrderRepository orders) {
        this.products = products; this.orders = orders;
    }
    @Transactional(readOnly = true)
    public DashboardResponse get() {
        long pending = orders.countByStatus(OrderStatus.PENDING);
        long confirmed = orders.countByStatus(OrderStatus.CONFIRMED);
        long cancelled = orders.countByStatus(OrderStatus.CANCELLED);
        // MVP: revenue is based on currently CONFIRMED orders, cancellations excluded.
        BigDecimal totalRevenue = orders.findAllByStatus(OrderStatus.CONFIRMED).stream()
                .map(SaleOrder::getTotalAmountCny).reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate fromDate = today.minusDays(6);
        Instant from = fromDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant to = today.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        Map<LocalDate, List<SaleOrder>> byDate = orders
                .findAllByStatusAndConfirmedAtGreaterThanEqualAndConfirmedAtLessThan(OrderStatus.CONFIRMED, from, to)
                .stream().collect(Collectors.groupingBy(o -> o.getConfirmedAt().atZone(BUSINESS_ZONE).toLocalDate()));
        List<DailyRevenue> daily = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = fromDate.plusDays(i);
            List<SaleOrder> dailyOrders = byDate.getOrDefault(date, List.of());
            BigDecimal sum = dailyOrders.stream().map(SaleOrder::getTotalAmountCny)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            daily.add(new DailyRevenue(date, dailyOrders.size(), sum));
        }
        return new DashboardResponse(products.findAllByActiveTrueOrderByIdAsc().size(), products.findLowStock().size(),
                pending, confirmed, cancelled, totalRevenue, daily);
    }
}
