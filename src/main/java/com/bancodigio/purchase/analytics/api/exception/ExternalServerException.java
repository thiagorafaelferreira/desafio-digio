package com.bancodigio.purchase.analytics.api.exception;

public class ExternalServerException extends RuntimeException {
    public ExternalServerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
