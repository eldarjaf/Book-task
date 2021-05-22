package com.eldar.workdaytask.task.config;

public class DuplicateTitleException extends RuntimeException {
    public DuplicateTitleException(String message) {
        super(message);
    }

    public DuplicateTitleException(String message, Throwable cause) {
        super(message, cause);
    }
}
