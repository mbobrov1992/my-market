package ru.yandex.my.market.model.dto;

import ru.yandex.my.market.model.enums.CartItemAction;

public record CartItemUpdateForm(
        Long id,
        CartItemAction action
) {
}
