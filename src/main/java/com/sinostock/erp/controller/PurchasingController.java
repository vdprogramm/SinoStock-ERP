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
import java.util.*;
@RestController @RequestMapping("/api/purchasing")
public class PurchasingController {
 private final PurchaseOrderRepository purchases;private final SupplierRepository suppliers;
 private final ProductRepository products;private final InventoryLogRepository logs;
 public PurchasingController(PurchaseOrderRepository purchases,SupplierRepository suppliers,ProductRepository products,InventoryLogRepository logs){
 this.purchases=purchases;this.suppliers=suppliers;this.products=products;this.logs=logs;
 }
 public record PurchaseRequest(@NotNull Long supplierId,@NotNull Long productId,@NotNull @Min(1) Integer quantity,@NotNull @DecimalMin("0.01") BigDecimal unitCostCny){}
 public record PurchaseView(Long id,String reference,String supplier,String sku,int quantity,BigDecimal unitCostCny,PurchaseStatus status,Instant createdAt){}
 private PurchaseView view(PurchaseOrder p){return new PurchaseView(p.getId(),p.getReference(),p.getSupplier().getName(),p.getProduct().getSku(),p.getQuantity(),p.getUnitCostCny(),p.getStatus(),p.getCreatedAt());}
 @GetMapping @Transactional(readOnly=true)
 public List<PurchaseView> list(){return purchases.findAll().stream().map(this::view).toList();}
 @PostMapping @Transactional @ResponseStatus(HttpStatus.CREATED)
 public PurchaseView create(@Valid @RequestBody PurchaseRequest r){
 Supplier s=suppliers.findById(r.supplierId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy nhà cung cấp"));
 Product p=products.findById(r.productId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy sản phẩm"));
 if(!p.isActive()) throw new ApiException(HttpStatus.CONFLICT,"Sản phẩm đã ngừng kinh doanh");
 return view(purchases.save(new PurchaseOrder("PO-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(),s,p,r.quantity(),r.unitCostCny())));
 }
 @PatchMapping("/{id}/receive") @Transactional
 public PurchaseView receive(@PathVariable Long id){
 PurchaseOrder po=purchases.lockById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy phiếu mua hàng"));
 if(po.getStatus()!=PurchaseStatus.DRAFT)throw new ApiException(HttpStatus.CONFLICT,"Phiếu nhập không ở trạng thái chờ");
 Product p=products.findByIdForUpdate(po.getProduct().getId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy sản phẩm"));
 int stock;
 try{stock=Math.addExact(p.getStockQuantity(),po.getQuantity());}
 catch(ArithmeticException e){throw new ApiException(HttpStatus.CONFLICT,"Số lượng vượt giới hạn");}
 p.changeStock(stock);
 logs.save(new InventoryLog(p,null,po.getQuantity(),stock,"PURCHASE_RECEIVE"));
 po.receive();return view(po);
 }
 @PatchMapping("/{id}/cancel") @Transactional
 public PurchaseView cancel(@PathVariable Long id){
 PurchaseOrder po=purchases.lockById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy phiếu mua hàng"));
 if(po.getStatus()!=PurchaseStatus.DRAFT)throw new ApiException(HttpStatus.CONFLICT,"Chỉ được hủy phiếu chưa nhập kho");
 po.cancel();return view(po);
 }
}
