package com.daurenassanbaev.amazons3service.exceptions;

public class S3ServiceException extends RuntimeException {
    public S3ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}