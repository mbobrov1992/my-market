package ru.yandex.practicum.market.core.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemFilterForm {

    private String search = "";
    private int pageNumber = 0;
    private int pageSize = 5;
    private ItemSortType sort = ItemSortType.NO;
}
