package com.eldar.workdaytask.task.config;

public class NoResultFoundException extends RuntimeException {
    public NoResultFoundException(String message) {
        super(message);
    }

    public NoResultFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
