package ru.yandex.practicum.market.core.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.market.core.model.dto.CartItemDto;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceService {

    public BigDecimal calculatePrice(List<CartItemDto> cartItems) {
        return cartItems.stream()
                .map(cartItem -> {
                    BigDecimal count = BigDecimal.valueOf(cartItem.count());
                    BigDecimal price = cartItem.price();
                    return count.multiply(price);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
