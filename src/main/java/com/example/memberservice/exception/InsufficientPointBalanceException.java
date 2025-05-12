package com.example.memberservice.exception;

public class InsufficientPointBalanceException extends RuntimeException {
    public InsufficientPointBalanceException(String message) {
        super(message);
    }
}
