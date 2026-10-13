package com.sinostock.erp.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="purchase_orders")
public class PurchaseOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String reference;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) private Supplier supplier;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) private Product product;
 private int quantity;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal unitCostCny;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private PurchaseStatus status=PurchaseStatus.DRAFT;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
 private Instant receivedAt;
 protected PurchaseOrder(){}
 public PurchaseOrder(String reference,Supplier supplier,Product product,int quantity,BigDecimal unitCostCny){
 this.reference=reference;this.supplier=supplier;this.product=product;this.quantity=quantity;this.unitCostCny=unitCostCny;
 }
 public Long getId(){return id;} public String getReference(){return reference;} public Supplier getSupplier(){return supplier;} public Product getProduct(){return product;}
 public int getQuantity(){return quantity;} public BigDecimal getUnitCostCny(){return unitCostCny;}
 public PurchaseStatus getStatus(){return status;} public Instant getCreatedAt(){return createdAt;} public Instant getReceivedAt(){return receivedAt;}
 public void receive(){status=PurchaseStatus.RECEIVED;receivedAt=Instant.now();}
 public void cancel(){status=PurchaseStatus.CANCELLED;}
}
