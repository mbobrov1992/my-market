package ru.yandex.my.market.model.dto;


import java.math.BigDecimal;

public record ItemCountDto(
        long id,
        String title,
        String imagePath,
        String description,
        BigDecimal price,
        int count
) {
    public ItemCountDto(ItemDto item, int count) {
        this(
                item.id(),
                item.title(),
                item.imagePath(),
                item.description(),
                item.price(),
                count
        );
    }

    public static final ItemCountDto MOCK = new ItemCountDto(
            -1,
            null,
            null,
            null,
            null,
            0
    );
}
