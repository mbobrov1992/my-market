package ru.yandex.my.market.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.my.market.model.dto.ItemDto;
import ru.yandex.my.market.model.entity.ItemEnt;

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
