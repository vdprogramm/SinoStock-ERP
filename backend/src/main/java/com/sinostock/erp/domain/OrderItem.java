package com.sinostock.erp.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private SaleOrder order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPriceCny;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotalCny;

    protected OrderItem() {}
    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPriceCny = product.getPriceCny();
        this.lineTotalCny = unitPriceCny.multiply(BigDecimal.valueOf(quantity));
    }
    public Long getId() { return id; }
    public SaleOrder getOrder() { return order; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPriceCny() { return unitPriceCny; }
    public BigDecimal getLineTotalCny() { return lineTotalCny; }
    void setOrder(SaleOrder order) { this.order = order; }
}
