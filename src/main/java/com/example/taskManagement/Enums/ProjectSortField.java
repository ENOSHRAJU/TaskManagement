package com.example.taskManagement.Enums;

public enum ProjectSortField {
    NAME("name"),
    STATUS("status"),
    CREATED_AT("createdAt");

    private final String field;

    ProjectSortField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
