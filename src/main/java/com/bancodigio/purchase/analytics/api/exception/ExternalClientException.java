package com.bancodigio.purchase.analytics.api.exception;

public class ExternalClientException extends RuntimeException {
    public ExternalClientException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
