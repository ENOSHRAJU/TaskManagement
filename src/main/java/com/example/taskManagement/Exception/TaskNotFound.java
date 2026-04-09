package com.example.taskManagement.Exception;

public class TaskNotFound extends RuntimeException {
    public TaskNotFound(String message) {
        super(message);
    }
}
