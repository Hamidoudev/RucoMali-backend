package com.malirussia.transfer.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String cleMessage;

    public ApiException(HttpStatus status, String cleMessage) {
        super(cleMessage);
        this.status = status;
        this.cleMessage = cleMessage;
    }

    public HttpStatus getStatus() { return status; }
    public String getCleMessage() { return cleMessage; }
}
