package ru.yandex.practicum.market.core.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartViewDto(
        List<CartItemDto> cartItems,
        BigDecimal totalPrice,
        PaymentInfo paymentInfo
) {
}
