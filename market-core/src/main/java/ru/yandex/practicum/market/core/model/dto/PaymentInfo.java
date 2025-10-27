package ru.yandex.practicum.market.core.model.dto;

import java.math.BigDecimal;

public record PaymentInfo(
        BigDecimal balance,
        boolean serviceAvailable
) {
}
