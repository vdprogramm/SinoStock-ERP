package com.sinostock.erp.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="payments")
public class Payment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) private SaleOrder order;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal amountCny;
 @Column(nullable=false,length=40) private String method;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
 protected Payment(){}
 public Payment(SaleOrder order,BigDecimal amountCny,String method){this.order=order;this.amountCny=amountCny;this.method=method;}
 public Long getId(){return id;}public SaleOrder getOrder(){return order;}public BigDecimal getAmountCny(){return amountCny;}
 public String getMethod(){return method;}public Instant getCreatedAt(){return createdAt;}
}
