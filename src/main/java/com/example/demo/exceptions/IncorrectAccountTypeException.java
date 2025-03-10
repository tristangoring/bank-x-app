package com.example.demo.exceptions;

public class IncorrectAccountTypeException extends RuntimeException {
    public IncorrectAccountTypeException(String message) {
        super(message);
    }
}
