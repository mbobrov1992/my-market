package ru.yandex.practicum.market.core.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        long id,
        List<OrderItemDto> items,
        BigDecimal totalSum
) {
}
