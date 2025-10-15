package com.example.exception;

public final class UnderageException extends DomainException {
    public UnderageException() { super("UNDERAGE", "User must be 18+."); }
}