package com.sinostock.erp.controller;

import com.sinostock.erp.dto.ApiDtos.*;
import com.sinostock.erp.repository.InventoryLogRepository;
import com.sinostock.erp.service.ProductService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/inventory")
public class InventoryController {
    private final ProductService products;
    private final InventoryLogRepository logs;
    public InventoryController(ProductService products, InventoryLogRepository logs) {
        this.products = products; this.logs = logs;
    }
    @GetMapping("/low-stock") public List<ProductResponse> lowStock() { return products.lowStock(); }
    @GetMapping("/logs") @Transactional(readOnly = true)
    public List<InventoryLogResponse> logs() {
        return logs.findTop100ByOrderByCreatedAtDesc().stream()
                .map(log -> new InventoryLogResponse(log.getId(), log.getProduct().getSku(),
                        log.getOrder() == null ? null : log.getOrder().getOrderNumber(),
                        log.getChangeQuantity(), log.getResultingStock(), log.getReason(), log.getCreatedAt()))
                .toList();
    }
}
