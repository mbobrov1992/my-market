package ru.yandex.practicum.market.core.exception;

public class ItemNotFoundException extends ResourceNotFoundException {

    public ItemNotFoundException(Long orderId) {
        super("Не найден товар с id: " + orderId);
    }
}
