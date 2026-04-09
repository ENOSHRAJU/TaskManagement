package com.example.taskManagement.DTOs;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "name"
})
public class UserSummaryDTO {
    private Long id;
    private String name;
}
