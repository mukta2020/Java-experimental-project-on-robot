package com.robot.grid.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex,
                                              HttpServletRequest req) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        return error(HttpStatus.BAD_REQUEST, errors, req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadArgument(IllegalArgumentException ex,
                                               HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, List.of(ex.getMessage()), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
            List.of("An unexpected error occurred"), req.getRequestURI());
    }

    private ResponseEntity<?> error(HttpStatus status, List<String> messages, String path) {
        return ResponseEntity.status(status).body(Map.of(
            "timestamp", Instant.now(),
            "status",    status.value(),
            "errors",    messages,
            "path",      path
        ));
    }
}
