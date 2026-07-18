package com.example.taskManagement.Exception;

public class InActiveUserException extends RuntimeException {
    public InActiveUserException(String message) {
        super(message);
    }
}
