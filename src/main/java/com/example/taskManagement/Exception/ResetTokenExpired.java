package com.example.taskManagement.Exception;

public class ResetTokenExpired extends RuntimeException {
    public ResetTokenExpired(String message) {
        super(message);
    }
}
