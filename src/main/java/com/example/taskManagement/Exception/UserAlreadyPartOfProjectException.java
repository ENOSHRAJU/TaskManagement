package com.example.taskManagement.Exception;

public class UserAlreadyPartOfProjectException extends RuntimeException {
    public UserAlreadyPartOfProjectException(String message) {
        super(message);
    }
}
