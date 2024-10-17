package com.daurenassanbaev.userservice.exceptions;

public class InvalidImageFileException extends RuntimeException {
    public InvalidImageFileException(String message) {
        super(message);
    }
}
