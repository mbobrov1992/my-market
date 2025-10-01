package ru.yandex.my.market.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.my.market.model.dto.OrderItemDto;
import ru.yandex.my.market.model.entity.OrderItemEnt;

@Component
public class OrderItemMapper {

    public OrderItemDto toDto(OrderItemEnt entity) {
        return new OrderItemDto(
                entity.getItem().getTitle(),
                entity.getCount(),
                entity.getPrice()
        );
    }
}
