package ru.practicum.shareit.exception;

public class ForbiddenException extends RuntimeException {
    private final String message;

    public ForbiddenException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
