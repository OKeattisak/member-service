package com.example.memberservice.exception;

public class PrivilegeNotAuthorizedException extends RuntimeException {
    public PrivilegeNotAuthorizedException(String message) {
        super(message);
    }
}
