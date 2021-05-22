package com.eldar.workdaytask.task.config;

public class DuplicateTitleResponse {
    private String errorMessage;

    public DuplicateTitleResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getError() {
        return "Title exists";
    }

    public String getMessage() {
        return errorMessage;
    }
}
