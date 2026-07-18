package com.example.taskManagement.Exception;

public class TaskDoesNotBelongToSameProject extends RuntimeException {
    public TaskDoesNotBelongToSameProject(String message) {
        super(message);
    }
}
