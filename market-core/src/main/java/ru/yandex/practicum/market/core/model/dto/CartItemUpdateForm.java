package ru.yandex.practicum.market.core.model.dto;

import ru.yandex.practicum.market.core.model.enums.CartItemAction;

public record CartItemUpdateForm(
        Long id,
        CartItemAction action
) {
}
