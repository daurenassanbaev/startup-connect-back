package com.daurenassanbaev.notificationservice.exceptions;

public class InvalidMessageFormatException extends RuntimeException {
    public InvalidMessageFormatException(String invalidMessageFormat) {
        super(invalidMessageFormat);
    }
}
