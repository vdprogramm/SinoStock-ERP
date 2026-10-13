package com.sinostock.erp.service;

import com.sinostock.erp.domain.InventoryLog;
import com.sinostock.erp.domain.Product;
import com.sinostock.erp.dto.ApiDtos.*;
import com.sinostock.erp.exception.ApiException;
import com.sinostock.erp.repository.InventoryLogRepository;
import com.sinostock.erp.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository products;
    private final InventoryLogRepository logs;
    public ProductService(ProductRepository products, InventoryLogRepository logs) {
        this.products = products; this.logs = logs;
    }
    @Transactional(readOnly = true)
    public List<ProductResponse> list() { return products.findAllByActiveTrueOrderByIdAsc().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly = true)
    public List<ProductResponse> lowStock() { return products.findLowStock().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) { return toResponse(require(id)); }

    @Transactional
    public ProductResponse create(ProductRequest dto) {
        products.findBySku(dto.sku().trim()).ifPresent(p -> { throw new ApiException(HttpStatus.CONFLICT, "SKU already exists"); });
        Product p = new Product(dto.sku().trim(), dto.nameCn().trim(), dto.nameEn().trim(),
                dto.priceCny(), dto.stockQuantity(), dto.lowStockThreshold());
        return toResponse(products.save(p));
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest dto) {
        Product p = products.findByIdForUpdate(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        products.findBySku(dto.sku().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw new ApiException(HttpStatus.CONFLICT, "SKU already exists");
        });
        p.edit(dto.sku().trim(), dto.nameCn().trim(), dto.nameEn().trim(), dto.priceCny(), dto.lowStockThreshold());
        return toResponse(p);
    }

    @Transactional
    public ProductResponse adjustStock(Long id, StockAdjustRequest dto) {
        Product p = products.findByIdForUpdate(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!p.isActive()) throw new ApiException(HttpStatus.CONFLICT, "Product is inactive");
        if (dto.changeQuantity() == 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Change quantity must be nonzero");
        int newStock;
        try { newStock = Math.addExact(p.getStockQuantity(), dto.changeQuantity()); }
        catch (ArithmeticException ex) { throw new ApiException(HttpStatus.BAD_REQUEST, "Stock quantity overflow"); }
        if (newStock < 0) throw new ApiException(HttpStatus.CONFLICT, "Insufficient stock");
        p.changeStock(newStock);
        logs.save(new InventoryLog(p, null, dto.changeQuantity(), newStock, "MANUAL:" + dto.reason().trim()));
        return toResponse(p);
    }

    @Transactional
    public void deactivate(Long id) {
        Product p = products.findByIdForUpdate(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        p.deactivate();
    }

    private Product require(Long id) {
        return products.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }
    public ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getSku(), p.getNameCn(), p.getNameEn(),
                p.getPriceCny(), p.getStockQuantity(), p.getLowStockThreshold(), p.isActive());
    }
}
