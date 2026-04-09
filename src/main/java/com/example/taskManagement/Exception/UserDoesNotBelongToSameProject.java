package com.example.taskManagement.Exception;

public class UserDoesNotBelongToSameProject extends RuntimeException {
    public UserDoesNotBelongToSameProject(String message) {
        super(message);
    }
}
