package ru.yandex.practicum.market.core.model.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import static org.springframework.data.domain.Sort.Direction.ASC;

@RequiredArgsConstructor
public enum ItemSortType {

    NO(null),
    ALPHA("title"),
    PRICE("price");

    private final String fieldName;

    public Sort getSort() {
        return this == NO
                ? Sort.unsorted()
                : Sort.by(ASC, fieldName);
    }
}
