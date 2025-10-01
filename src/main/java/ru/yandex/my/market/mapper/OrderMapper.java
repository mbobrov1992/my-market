package ru.yandex.my.market.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.my.market.model.dto.OrderDto;
import ru.yandex.my.market.model.entity.OrderEnt;

@RequiredArgsConstructor
@Component
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;

    public OrderDto toDto(OrderEnt entity) {
        return new OrderDto(
                entity.getId(),
                entity.getItems()
                        .stream()
                        .map(orderItemMapper::toDto)
                        .toList(),
                entity.getTotalPrice()
        );
    }
}
