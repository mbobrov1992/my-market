package ru.yandex.my.market.model.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        String title,
        int count,
        BigDecimal price
) {
}
