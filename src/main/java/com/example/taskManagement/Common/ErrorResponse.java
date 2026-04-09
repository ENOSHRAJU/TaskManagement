package com.example.taskManagement.Common;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timeStamp;
    private String path;
    private List<String> details;
}
