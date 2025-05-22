package com.example.memberservice.exception;

public class MemberLevelNotFoundException extends RuntimeException {
    public MemberLevelNotFoundException(String message) {
        super(message);
    }
}
