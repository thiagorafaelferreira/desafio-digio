package com.bancodigio.purchase.analytics.api.exception;

public record ApiError(String code, String message) {
    static ApiError of(String code, String message) {
        return new ApiError(code, message);
    }
}
