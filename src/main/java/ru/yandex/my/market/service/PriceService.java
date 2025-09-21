package ru.yandex.my.market.service;

import org.springframework.stereotype.Service;
import ru.yandex.my.market.model.dto.ItemCountDto;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceService {

    public BigDecimal calculatePrice(List<ItemCountDto> itemCounts) {
        return itemCounts.stream()
                .map(itemCount -> {
                    BigDecimal count = BigDecimal.valueOf(itemCount.count());
                    BigDecimal price = itemCount.price();
                    return count.multiply(price);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
