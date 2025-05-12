package com.example.memberservice.exception;

public class PointBalanceNotFoundException extends RuntimeException {
    public PointBalanceNotFoundException(Long memberId) {
        super("No point balance found for member " + memberId);
    }

    public PointBalanceNotFoundException(String message) {
        super(message);
    }
}
