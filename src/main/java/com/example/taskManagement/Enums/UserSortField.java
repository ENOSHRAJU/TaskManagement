package com.example.taskManagement.Enums;

public enum UserSortField {
    NAME("name"),
    EMAIL("email");

    private final String field;

    UserSortField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
