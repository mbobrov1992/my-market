package ru.yandex.practicum.market.core.model.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        String title,
        int count,
        BigDecimal price
) {
}
