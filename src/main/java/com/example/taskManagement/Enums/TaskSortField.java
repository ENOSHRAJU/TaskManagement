package com.example.taskManagement.Enums;

public enum TaskSortField {
    TITLE("title"),
    STATUS("status"),
    PRIORITY("taskPriority"),
    DUE_DATE("dueDate"),
    CREATED_AT("createdAt");

    public final String field;

    TaskSortField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
