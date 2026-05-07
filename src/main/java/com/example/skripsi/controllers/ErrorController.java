package com.example.skripsi.controllers;

import com.example.skripsi.exceptions.*;
import com.example.skripsi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorController {
    private static final Logger log = LoggerFactory.getLogger(ErrorController.class);

    public ResponseEntity<WebResponse<?>> handleNotFound(NoHandlerFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message("Not Found")
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<WebResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message("Access Denied")
                        .build());
    }

    @ExceptionHandler(CustomAccessDeniedException.class)
    public ResponseEntity<WebResponse<?>> customHandleAccessDenied(CustomAccessDeniedException ex) {
        log.warn("Custom access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message("Access Denied")
                        .build());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<WebResponse<String>> handleInvalidCredentials(InvalidCredentialsException ex){
        log.warn("Invalid credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(BadRequestExceptions.class)
    public ResponseEntity<WebResponse<String>> handleBadRequest(BadRequestExceptions ex){
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<WebResponse<String>> handleInvalidRefreshToken(InvalidTokenException ex) {
        log.warn("Invalid token: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<WebResponse<String>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<WebResponse<String>> handleValidationError(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<WebResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(WebResponse.builder()
                        .success(false)
                        .message(errorMessage)
                        .build());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<WebResponse<String>> apiException(ResponseStatusException ex){
        return ResponseEntity.status(ex.getStatusCode())
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(ex.getReason())
                        .build());
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<WebResponse<?>> handleCompletionException(CompletionException ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        String causeMsg = cause.getMessage();
        log.error("Async operation failed", cause);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(WebResponse.builder()
                        .success(false)
                        .message("Async operation failed: " + causeMsg)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WebResponse<?>> handleGeneralException(Exception ex){
        log.error("Unhandled exception", ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred. Please try again later.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(WebResponse.builder()
                        .success(false)
                        .message(message)
                        .build());
    }
}
