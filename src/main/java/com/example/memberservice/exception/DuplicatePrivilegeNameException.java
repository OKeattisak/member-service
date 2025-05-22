package com.example.memberservice.exception;

public class DuplicatePrivilegeNameException extends RuntimeException {
    public DuplicatePrivilegeNameException(String message) {
        super(message);
    }
}
