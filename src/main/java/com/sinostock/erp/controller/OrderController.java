package com.sinostock.erp.controller;

import com.sinostock.erp.dto.ApiDtos.*;
import com.sinostock.erp.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }
    @GetMapping public List<OrderResponse> list() { return service.list(); }
    @GetMapping("/{id}") public OrderResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request, Authentication auth) {
        return service.create(request, auth.getName());
    }
    @PatchMapping("/{id}/confirm") public OrderResponse confirm(@PathVariable Long id) { return service.confirm(id); }
    @PatchMapping("/{id}/cancel") public OrderResponse cancel(@PathVariable Long id) { return service.cancel(id); }
}
