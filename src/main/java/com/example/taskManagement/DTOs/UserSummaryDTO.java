package com.example.taskManagement.DTOs;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "name"
})
public class UserSummaryDTO {
    private UUID id;
    private String name;
}
