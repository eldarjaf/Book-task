package com.eldar.workdaytask.task.config;

public class NoResultFoundResponse {
    private String errorMessage;

    public NoResultFoundResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getError() {
        return "No results found";
    }

    public String getMessage() {
        return errorMessage;
    }
}
