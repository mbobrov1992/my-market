package ru.yandex.practicum.payment.exception;

public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super("Невозможно произвести оплату. " + message);
    }
}
