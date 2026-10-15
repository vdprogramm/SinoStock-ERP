package com.sinostock.erp.controller;
import com.sinostock.erp.domain.*;
import com.sinostock.erp.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
public class PartnerController {
 private final SupplierRepository suppliers;private final CustomerRepository customers;
 public PartnerController(SupplierRepository suppliers,CustomerRepository customers){this.suppliers=suppliers;this.customers=customers;}
 public record PartnerRequest(@NotBlank String name,String phone,String email,String address){}
 public record PartnerView(Long id,String name,String phone,String email,String address){}
 @GetMapping("/api/suppliers") @Transactional(readOnly=true)
 public List<PartnerView> suppliers(){return suppliers.findAll().stream().map(s->new PartnerView(s.getId(),s.getName(),s.getPhone(),s.getEmail(),s.getAddress())).toList();}
 @PostMapping("/api/suppliers") @Transactional @ResponseStatus(HttpStatus.CREATED)
 public PartnerView addSupplier(@Valid @RequestBody PartnerRequest r){Supplier s=suppliers.save(new Supplier(r.name().trim(),r.phone(),r.email(),r.address()));return new PartnerView(s.getId(),s.getName(),s.getPhone(),s.getEmail(),s.getAddress());}
 @GetMapping("/api/customers") @Transactional(readOnly=true)
 public List<PartnerView> customers(){return customers.findAll().stream().map(c->new PartnerView(c.getId(),c.getName(),c.getPhone(),c.getEmail(),c.getAddress())).toList();}
 @PostMapping("/api/customers") @Transactional @ResponseStatus(HttpStatus.CREATED)
 public PartnerView addCustomer(@Valid @RequestBody PartnerRequest r){Customer c=customers.save(new Customer(r.name().trim(),r.phone(),r.email(),r.address()));return new PartnerView(c.getId(),c.getName(),c.getPhone(),c.getEmail(),c.getAddress());}
}
