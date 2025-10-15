package com.example.exception;

public final class NonZeroBalanceDeletionException extends DomainException {
    public NonZeroBalanceDeletionException(String message) { super("NON_ZERO_BALANCE", message); }
}