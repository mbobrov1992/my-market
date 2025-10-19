package ru.yandex.practicum.market.core.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.market.core.model.dto.ItemDto;
import ru.yandex.practicum.market.core.model.entity.ItemEnt;

@Component
public class ItemMapper {

    public ItemDto toDto(ItemEnt entity) {
        return new ItemDto(
                entity.getId(),
                entity.getTitle(),
                entity.getImagePath(),
                entity.getDescription(),
                entity.getPrice()
        );
    }
}
