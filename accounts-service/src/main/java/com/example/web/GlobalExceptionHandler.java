package com.example.web;

import com.example.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(String code, String message, Instant timestamp) {}

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler({ConflictException.class})
    ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler({ValidationException.class, UnderageException.class})
    ResponseEntity<ApiError> handleValidation(DomainException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler({ForbiddenOperationException.class, NonZeroBalanceDeletionException.class})
    ResponseEntity<ApiError> handleForbidden(DomainException ex) {
        return build(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiError> handleDomain(DomainException ex) {
        return build(HttpStatus.BAD_REQUEST, ex);
    }

    // === WebClient (reactive) исключения ===
    @ExceptionHandler(org.springframework.web.reactive.function.client.WebClientResponseException.class)
    ResponseEntity<ApiError> handleWebClient(org.springframework.web.reactive.function.client.WebClientResponseException ex) {
        HttpStatus status;
        if (ex.getStatusCode().is4xxClientError()) {
            status = HttpStatus.BAD_GATEWAY; // прокси-ошибка к апстриму — как у тебя было
        } else if (ex.getStatusCode().is5xxServerError()) {
            status = HttpStatus.BAD_GATEWAY;
        } else {
            status = HttpStatus.BAD_GATEWAY;
        }
        var msg = "Upstream " + ex.getRawStatusCode() + ": " + ex.getResponseBodyAsString();
        return ResponseEntity.status(status).body(new ApiError("validation", msg, Instant.now()));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, DomainException ex) {
        return ResponseEntity.status(status).body(new ApiError(ex.code(), ex.getMessage(), Instant.now()));
    }
}
