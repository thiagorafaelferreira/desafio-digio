package com.bancodigio.purchase.analytics.api.exception;

public class ExternalNotFoundOrBadRequest extends RuntimeException {
    public ExternalNotFoundOrBadRequest(String message, Throwable throwable) {
        super(message, throwable);
    }
}
