package com.sinostock.erp.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String sku;
    @Column(nullable = false, length = 200)
    private String nameCn;
    @Column(nullable = false, length = 200)
    private String nameEn;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal priceCny;
    @Column(nullable = false)
    private int stockQuantity;
    @Column(nullable = false)
    private int lowStockThreshold;
    @Column(nullable = false)
    private boolean active = true;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Product() {}
    public Product(String sku, String nameCn, String nameEn, BigDecimal priceCny, int stockQuantity, int lowStockThreshold) {
        this.sku = sku;
        this.nameCn = nameCn;
        this.nameEn = nameEn;
        this.priceCny = priceCny;
        this.stockQuantity = stockQuantity;
        this.lowStockThreshold = lowStockThreshold;
    }
    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getNameCn() { return nameCn; }
    public String getNameEn() { return nameEn; }
    public BigDecimal getPriceCny() { return priceCny; }
    public int getStockQuantity() { return stockQuantity; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public void edit(String sku, String nameCn, String nameEn, BigDecimal priceCny, int lowStockThreshold) {
        this.sku = sku; this.nameCn = nameCn; this.nameEn = nameEn;
        this.priceCny = priceCny; this.lowStockThreshold = lowStockThreshold;
    }
    public void changeStock(int newStock) { this.stockQuantity = newStock; }
    public void deactivate() { this.active = false; }
}
