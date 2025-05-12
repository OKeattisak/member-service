package com.example.memberservice.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(Long id) {
        super("Member not found with id " + id);
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}
