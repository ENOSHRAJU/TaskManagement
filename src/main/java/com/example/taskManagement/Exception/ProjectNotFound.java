package com.example.taskManagement.Exception;

public class ProjectNotFound extends RuntimeException {
    public ProjectNotFound(String message) {
        super(message);
    }
}
