package ru.yandex.practicum.market.core.model.dto;

import java.math.BigDecimal;

public record CartItemDto(
        long id,
        String title,
        String imagePath,
        String description,
        BigDecimal price,
        int count
) {
    public CartItemDto(ItemDto item, int count) {
        this(
                item.id(),
                item.title(),
                item.imagePath(),
                item.description(),
                item.price(),
                count
        );
    }

    public static final CartItemDto MOCK = new CartItemDto(
            -1,
            null,
            null,
            null,
            null,
            0
    );
}
