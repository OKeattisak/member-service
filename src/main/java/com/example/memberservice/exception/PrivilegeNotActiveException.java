package com.example.memberservice.exception;

public class PrivilegeNotActiveException extends RuntimeException {
    public PrivilegeNotActiveException(String message) {
        super(message);
    }
}
