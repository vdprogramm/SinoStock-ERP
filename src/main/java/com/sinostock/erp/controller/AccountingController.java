package com.sinostock.erp.controller;
import com.sinostock.erp.domain.*;
import com.sinostock.erp.exception.ApiException;
import com.sinostock.erp.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
@RestController @RequestMapping("/api/accounting")
public class AccountingController {
 private final OrderRepository orders;private final PaymentRepository payments;
 public AccountingController(OrderRepository orders,PaymentRepository payments){this.orders=orders;this.payments=payments;}
 public record PaymentRequest(@NotNull Long orderId,@NotNull @DecimalMin("0.01") BigDecimal amountCny,@NotBlank String method){}
 public record PaymentView(Long id,String orderNumber,BigDecimal amountCny,String method,Instant createdAt){}
 private PaymentView view(Payment p){return new PaymentView(p.getId(),p.getOrder().getOrderNumber(),p.getAmountCny(),p.getMethod(),p.getCreatedAt());}
 @GetMapping("/payments") @Transactional(readOnly=true)
 public List<PaymentView> list(){return payments.findAll().stream().map(this::view).toList();}
 @PostMapping("/payments") @Transactional @ResponseStatus(HttpStatus.CREATED)
 public PaymentView collect(@Valid @RequestBody PaymentRequest req){
 SaleOrder order=orders.findByIdForUpdate(req.orderId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy đơn hàng"));
 if(order.getStatus()!=OrderStatus.CONFIRMED)throw new ApiException(HttpStatus.CONFLICT,"Chỉ thu tiền cho đơn đã xác nhận");
 BigDecimal paid=payments.findByOrderId(order.getId()).stream().map(Payment::getAmountCny).reduce(BigDecimal.ZERO,BigDecimal::add);
 if(paid.add(req.amountCny()).compareTo(order.getTotalAmountCny())>0)throw new ApiException(HttpStatus.CONFLICT,"Số tiền thu vượt công nợ của đơn");
 return view(payments.save(new Payment(order,req.amountCny(),req.method().trim())));
 }
 @GetMapping("/receivables") @Transactional(readOnly=true)
 public List<ReceivableView> receivables(){
 return orders.findAllByOrderByCreatedAtDesc().stream().filter(o->o.getStatus()==OrderStatus.CONFIRMED)
  .map(o->{BigDecimal paid=payments.findByOrderId(o.getId()).stream().map(Payment::getAmountCny).reduce(BigDecimal.ZERO,BigDecimal::add);
  return new ReceivableView(o.getId(),o.getOrderNumber(),o.getTotalAmountCny(),paid,o.getTotalAmountCny().subtract(paid));}).toList();
 }
 public record ReceivableView(Long orderId,String orderNumber,BigDecimal total,BigDecimal paid,BigDecimal outstanding){}
}
