package com.example.taskManagement.Exception;

public class UserNotAssignedToProject extends RuntimeException {
    public UserNotAssignedToProject(String message) {
        super(message);
    }
}
