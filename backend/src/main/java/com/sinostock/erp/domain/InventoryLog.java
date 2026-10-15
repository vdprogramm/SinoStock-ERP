package com.sinostock.erp.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inventory_logs")
public class InventoryLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private SaleOrder order;
    @Column(nullable = false)
    private int changeQuantity;
    @Column(nullable = false)
    private int resultingStock;
    @Column(nullable = false, length = 30)
    private String reason;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected InventoryLog() {}
    public InventoryLog(Product product, SaleOrder order, int changeQuantity, int resultingStock, String reason) {
        this.product = product; this.order = order; this.changeQuantity = changeQuantity;
        this.resultingStock = resultingStock; this.reason = reason;
    }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public SaleOrder getOrder() { return order; }
    public int getChangeQuantity() { return changeQuantity; }
    public int getResultingStock() { return resultingStock; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
