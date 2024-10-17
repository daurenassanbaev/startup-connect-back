package com.daurenassanbaev.notificationservice.handler;

import com.daurenassanbaev.notificationservice.exceptions.InvalidMessageFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidMessageFormatException.class)
    public ResponseEntity<?> handleInvalidMessageFormatException(InvalidMessageFormatException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
