package com.example.taskManagement.Exception;

public class InvalidProjectStatusTransition extends RuntimeException {
    public InvalidProjectStatusTransition(String message) {
        super(message);
    }
}
