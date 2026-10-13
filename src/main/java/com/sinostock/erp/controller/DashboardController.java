package com.sinostock.erp.controller;

import com.sinostock.erp.dto.ApiDtos.DashboardResponse;
import com.sinostock.erp.service.DashboardService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }
    @GetMapping public DashboardResponse get() { return service.get(); }
}
