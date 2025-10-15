package com.bancodigio.purchase.analytics.api.exception;

public class ExternalUnavailableException extends RuntimeException {
    public ExternalUnavailableException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
