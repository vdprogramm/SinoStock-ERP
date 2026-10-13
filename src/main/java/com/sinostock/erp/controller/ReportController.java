package com.sinostock.erp.controller;
import com.sinostock.erp.domain.*;
import com.sinostock.erp.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;
@RestController @RequestMapping("/api/reports")
public class ReportController {
 private final OrderRepository orders;private final PurchaseOrderRepository purchases;private final PaymentRepository payments;
 public ReportController(OrderRepository orders,PurchaseOrderRepository purchases,PaymentRepository payments){this.orders=orders;this.purchases=purchases;this.payments=payments;}
 @GetMapping("/summary") @Transactional(readOnly=true)
 public Map<String,Object> summary(){
 BigDecimal revenue=orders.findAllByStatus(OrderStatus.CONFIRMED).stream().map(SaleOrder::getTotalAmountCny).reduce(BigDecimal.ZERO,BigDecimal::add);
 BigDecimal collected=payments.findAll().stream().map(Payment::getAmountCny).reduce(BigDecimal.ZERO,BigDecimal::add);
 BigDecimal purchased=purchases.findAll().stream().filter(p->p.getStatus()==PurchaseStatus.RECEIVED)
 .map(p->p.getUnitCostCny().multiply(BigDecimal.valueOf(p.getQuantity()))).reduce(BigDecimal.ZERO,BigDecimal::add);
 return Map.of("confirmedRevenueCny",revenue,"recordedReceiptsCny",collected,"receivedPurchaseCostCny",purchased,
 "pendingReceivablesCny",revenue.subtract(collected),"totalOrders",orders.count(),"totalPurchaseOrders",purchases.count());
 }
}
