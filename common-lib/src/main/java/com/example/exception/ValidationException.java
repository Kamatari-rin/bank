package com.example.exception;

public final class ValidationException extends DomainException {
    public ValidationException(String message) { super("VALIDATION", message); }
}