package com.bancodigio.purchase.analytics.api.exception;

public class ExternalTimeoutException extends RuntimeException {
    public ExternalTimeoutException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
