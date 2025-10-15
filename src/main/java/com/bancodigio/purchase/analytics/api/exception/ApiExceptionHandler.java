package com.bancodigio.purchase.analytics.api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CompraNotFoundException.class)
    public ResponseEntity<ApiError> compraNotFoundException(CompraNotFoundException ex) {
        return ResponseEntity.status(404).body(ApiError.of("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ExternalClientException.class)
    public ResponseEntity<ApiError> clientException(RuntimeException ex) {
        return ResponseEntity.status(400).body(ApiError.of("EXTERNAL_4XX", ex.getMessage()));
    }

    @ExceptionHandler(ExternalServerException.class)
    public ResponseEntity<ApiError> serverException(RuntimeException ex) {
        return ResponseEntity.status(500).body(ApiError.of("EXTERNAL_5XX", ex.getMessage()));
    }

    @ExceptionHandler(ExternalNotFoundOrBadRequest.class)
    public ResponseEntity<ApiError> handle4xx(RuntimeException ex) {
        return ResponseEntity.status(422).body(ApiError.of("EXTERNAL_4XX", ex.getMessage()));
    }

    @ExceptionHandler(ExternalUnavailableException.class)
    public ResponseEntity<ApiError> handle5xx(RuntimeException ex) {
        return ResponseEntity.status(503).body(ApiError.of("EXTERNAL_5XX", ex.getMessage()));
    }

    @ExceptionHandler(ExternalTimeoutException.class)
    public ResponseEntity<ApiError> handleTimeout(RuntimeException ex) {
        return ResponseEntity.status(504).body(ApiError.of("EXTERNAL_TIMEOUT", ex.getMessage()));
    }
}
