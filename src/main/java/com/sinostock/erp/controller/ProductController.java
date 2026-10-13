package com.sinostock.erp.controller;

import com.sinostock.erp.dto.ApiDtos.*;
import com.sinostock.erp.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }
    @GetMapping public List<ProductResponse> list() { return service.list(); }
    @GetMapping("/{id}") public ProductResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) { return service.create(request); }
    @PutMapping("/{id}") public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return service.update(id, request);
    }
    @PatchMapping("/{id}/stock")
    public ProductResponse adjustStock(@PathVariable Long id, @Valid @RequestBody StockAdjustRequest request) {
        return service.adjustStock(id, request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) { service.deactivate(id); }
}
