package com.example.exception;

public final class ForbiddenOperationException extends DomainException {
    public ForbiddenOperationException(String message) { super("FORBIDDEN", message); }
}