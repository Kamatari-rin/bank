package com.example.exception;

public final class ConflictException extends DomainException {
    public ConflictException(String message) { super("CONFLICT", message); }
}