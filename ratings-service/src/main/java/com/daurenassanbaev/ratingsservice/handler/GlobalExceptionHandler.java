package com.daurenassanbaev.ratingsservice.handler;

import com.daurenassanbaev.ratingsservice.exceptions.RatingAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RatingAlreadyExistsException.class)
    public ResponseEntity<String> handleRatingAlreadyExistsException(RatingAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
