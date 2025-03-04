package com.juandmv.backend.exceptions;

public class InvalidDatesRangeException extends RuntimeException {
    public InvalidDatesRangeException(String message) {
        super(message);
    }
}
