package com.sinostock.erp.exception;

import com.sinostock.erp.dto.ApiDtos.ApiError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static String friendlyField(String field) {
        return switch (field) {
            case "username" -> "tên đăng nhập";
            case "password" -> "mật khẩu";
            case "sku" -> "mã SKU";
            case "nameCn", "nameEn" -> "tên sản phẩm";
            case "priceCny" -> "giá bán";
            case "stockQuantity" -> "số lượng tồn kho";
            case "lowStockThreshold" -> "ngưỡng cảnh báo";
            case "changeQuantity" -> "số lượng điều chỉnh";
            case "reason" -> "lý do";
            case "items" -> "danh sách sản phẩm";
            case "quantity" -> "số lượng";
            case "productId" -> "mã sản phẩm";
            default -> "dữ liệu";
        };
    }
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> api(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiError(ex.getMessage(), Instant.now()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(e -> "Thông tin " + friendlyField(e.getField()) + " không hợp lệ")
                .orElse("Dữ liệu gửi lên không hợp lệ");
        return ResponseEntity.badRequest().body(new ApiError(message, Instant.now()));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> invalidJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ApiError("Dữ liệu JSON không hợp lệ", Instant.now()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> constraint(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("Dữ liệu bị trùng hoặc tham chiếu không hợp lệ", Instant.now()));
    }
}
