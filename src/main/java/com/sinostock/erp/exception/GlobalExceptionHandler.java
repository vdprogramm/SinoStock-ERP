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
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> api(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiError(ex.getMessage(), Instant.now()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Invalid request");
        return ResponseEntity.badRequest().body(new ApiError(message, Instant.now()));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> invalidJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ApiError("Invalid JSON payload", Instant.now()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> constraint(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("Database constraint violation (duplicate value or invalid reference)", Instant.now()));
    }
}
