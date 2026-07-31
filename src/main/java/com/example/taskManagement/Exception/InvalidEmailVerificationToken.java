package com.example.taskManagement.Exception;

public class InvalidEmailVerificationToken extends RuntimeException {
  public InvalidEmailVerificationToken(String message) {
    super(message);
  }
}
