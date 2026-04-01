package com.example.skripsi.controllers;

import com.example.skripsi.exceptions.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.constant.*;
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

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<WebResponse<?>> handleNotFound(NoHandlerFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(MessageConstants.NotFound.ACCESS_DENIED_NOT_FOUND)
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<WebResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(MessageConstants.NotFound.ACCESS_DENIED)
                        .build());
    }

    @ExceptionHandler(CustomAccessDeniedException.class)
    public ResponseEntity<WebResponse<?>> customHandleAccessDenied(CustomAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(MessageConstants.NotFound.ACCESS_DENIED)
                        .build());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<WebResponse<String>> handleInvalidCredentials(InvalidCredentialsException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(BadRequestExceptions.class)
    public ResponseEntity<WebResponse<String>> handleBadRequest(BadRequestExceptions ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<WebResponse<String>> handleInvalidRefreshToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<WebResponse<String>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(WebResponse.<String>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<WebResponse<String>> handleValidationError(ValidationException ex) {
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

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(WebResponse.builder()
                        .success(false)
                        .message(MessageConstants.Async.ASYNC_OPERATION_FAILED + causeMsg)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WebResponse<?>> handleGeneralException(Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(WebResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }
}
