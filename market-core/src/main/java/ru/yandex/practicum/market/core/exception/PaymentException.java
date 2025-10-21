package ru.yandex.practicum.market.core.exception;

public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }
}
