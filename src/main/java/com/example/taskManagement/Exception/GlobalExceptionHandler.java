package com.example.taskManagement.Exception;

import com.example.taskManagement.Common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        LOGGER.error("Error occurred at {} : {}", request.getRequestURI(), ex.getMessage());
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation failure")
                .message("Invalid input data")
                .path(request.getRequestURI())
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request){

        LOGGER.error("Error occurred at {} : {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Invalid email or password")
                .path(request.getRequestURI())
                .details(List.of("Authentication failed"))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataValidations(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        LOGGER.error("Error occurred at {} : {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Data Integrity violation: ")
                .message("Database constraint violation")
                .path(request.getRequestURI())
                .details(List.of("Duplicate or invalid data"))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmails(
            DuplicateEmailException ex,
            HttpServletRequest request
    ) {

        LOGGER.error("Error occurred at {} : {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict: ")
                .message("Duplicate email entry")
                .path(request.getRequestURI())
                .details(List.of("Email already exists"))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        LOGGER.error("Unhandled exception at: {}", request.getRequestURI(), ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Something went wrong")
                .path(request.getRequestURI())
                .details(List.of("Unexpected error occurred. Please contact support"))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
