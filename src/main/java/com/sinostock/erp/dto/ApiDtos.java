package com.sinostock.erp.dto;

import com.sinostock.erp.domain.OrderStatus;
import com.sinostock.erp.domain.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String token, String tokenType, String username, Role role) {}
    public record ProductRequest(
            @NotBlank String sku,
            @NotBlank String nameCn,
            @NotBlank String nameEn,
            @NotNull @DecimalMin("0.01") BigDecimal priceCny,
            @NotNull @Min(0) Integer stockQuantity,
            @NotNull @Min(0) Integer lowStockThreshold) {}
    public record ProductUpdateRequest(
            @NotBlank String sku,
            @NotBlank String nameCn,
            @NotBlank String nameEn,
            @NotNull @DecimalMin("0.01") BigDecimal priceCny,
            @NotNull @Min(0) Integer lowStockThreshold) {}
    public record ProductResponse(Long id, String sku, String nameCn, String nameEn,
            BigDecimal priceCny, int stockQuantity, int lowStockThreshold, boolean active) {}
    public record StockAdjustRequest(@NotNull Integer changeQuantity, @NotBlank @Size(max=23) String reason) {}
    public record OrderLineRequest(@NotNull Long productId, @NotNull @Min(1) Integer quantity) {}
    public record CreateOrderRequest(@NotEmpty List<@Valid OrderLineRequest> items) {}
    public record OrderItemResponse(Long productId, String sku, String nameCn, int quantity,
            BigDecimal unitPriceCny, BigDecimal lineTotalCny) {}
    public record OrderResponse(Long id, String orderNumber, OrderStatus status,
            BigDecimal totalAmountCny, String createdBy, Instant createdAt, Instant confirmedAt,
            List<OrderItemResponse> items) {}
    public record InventoryLogResponse(Long id, String sku, String orderNumber,
            int changeQuantity, int resultingStock, String reason, Instant createdAt) {}
    public record DailyRevenue(LocalDate date, long orders, BigDecimal revenueCny) {}
    public record DashboardResponse(long totalProducts, long lowStockProducts,
            long pendingOrders, long confirmedOrders, long cancelledOrders,
            BigDecimal totalRevenueCny, List<DailyRevenue> dailyRevenue) {}
    public record ApiError(String message, Instant timestamp) {}
}
