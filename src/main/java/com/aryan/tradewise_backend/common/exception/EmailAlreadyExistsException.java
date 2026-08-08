package com.aryan.tradewise_backend.common.exception;

public class EmailAlreadyExistsException extends RuntimeException  {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
