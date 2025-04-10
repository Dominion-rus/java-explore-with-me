package ru.practicum.stats.dto.exceptions;

public class ValidateException extends RuntimeException {
    public ValidateException(String message) {
        super(message);
    }
}
