package com.sinostock.erp.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
public class SaleOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String orderNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmountCny = BigDecimal.ZERO;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private UserAccount createdBy;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    private Instant confirmedAt;

    protected SaleOrder() {}
    public SaleOrder(String orderNumber, UserAccount createdBy) {
        this.orderNumber = orderNumber;
        this.createdBy = createdBy;
    }
    public void addItem(OrderItem item) { item.setOrder(this); items.add(item); }
    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmountCny() { return totalAmountCny; }
    public UserAccount getCreatedBy() { return createdBy; }
    public List<OrderItem> getItems() { return items; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public void setTotalAmountCny(BigDecimal totalAmountCny) { this.totalAmountCny = totalAmountCny; }
    public void confirm() { this.status = OrderStatus.CONFIRMED; this.confirmedAt = Instant.now(); }
    public void cancel() { this.status = OrderStatus.CANCELLED; }
}
