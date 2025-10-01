package ru.yandex.my.market.exception;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long orderId) {
        super("Не найден заказ с id: " + orderId);
    }
}
