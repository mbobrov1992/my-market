package ru.yandex.practicum.market.core.exception;

public class CredentialsValidationException extends RuntimeException {

    public CredentialsValidationException(String message) {
        super(message);
    }
}
