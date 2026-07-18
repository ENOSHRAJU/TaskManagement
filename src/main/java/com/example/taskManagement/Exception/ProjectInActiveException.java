package com.example.taskManagement.Exception;

public class ProjectInActiveException extends RuntimeException {
    public ProjectInActiveException(String message) {
        super(message);
    }
}
