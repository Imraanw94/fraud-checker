package com.imraan.fraudcheck.boundary;

import com.imraan.fraudcheck.entity.dto.ApiError;
import com.imraan.fraudcheck.entity.dto.ApiError.FieldViolation;
import com.imraan.fraudcheck.entity.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(Exception ex) {
        String message = ex instanceof ResourceNotFoundException ? ex.getMessage() : "Resource not found";
        log.warn("Resource not found: {}", ex.getMessage());
        return error(HttpStatus.NOT_FOUND, message, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldViolation(e.getField(), e.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldViolation::field).thenComparing(FieldViolation::message))
                .toList();
        log.warn("Request validation failed: violations={}", violations);
        return badRequest("Validation failed", violations);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed or unreadable request body: {}", ex.getMostSpecificCause().getMessage());
        return badRequest("Malformed or unreadable request body", List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: method={}", ex.getMethod());
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Method '" + ex.getMethod() + "' is not supported", List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid request parameter: name={} value={}", ex.getName(), ex.getValue());
        return badRequest("Invalid value for parameter '" + ex.getName() + "'",
                List.of(new FieldViolation(ex.getName(), "invalid value")));
    }

    private ResponseEntity<ApiError> badRequest(String message, List<FieldViolation> violations) {
        return error(HttpStatus.BAD_REQUEST, message, violations);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, List<FieldViolation> violations) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, violations));
    }
}
