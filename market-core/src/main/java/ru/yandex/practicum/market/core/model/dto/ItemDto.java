package ru.yandex.practicum.market.core.model.dto;

import java.math.BigDecimal;

public record ItemDto(
        long id,
        String title,
        String imagePath,
        String description,
        BigDecimal price
) {
}
