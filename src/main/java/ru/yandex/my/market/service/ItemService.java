package ru.yandex.my.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.my.market.mapper.ItemMapper;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.entity.ItemEnt;
import ru.yandex.my.market.repository.ItemRepository;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemService {

    private final ItemRepository itemRepo;
    private final ItemMapper itemMapper;
    private final CartService cartService;

    @Transactional(readOnly = true)
    public Page<CartItemDto> getItems(String search, Pageable pageable) {
        log.info("Получаем товары по строке: \"{}\"", search);

        Page<ItemEnt> itemPage = itemRepo.findAllByTitleIsContainingIgnoreCaseOrDescriptionIsContainingIgnoreCase(
                search, search, pageable
        );

        List<Long> itemIds = itemPage.stream()
                .map(ItemEnt::getId)
                .toList();
        Map<Long, Integer> cartItemCount = cartService.getCartItemCount(itemIds);
        return itemPage
                .map(itemMapper::toDto)
                .map(dto -> {
                    Integer count = cartItemCount.get(dto.id());
                    return new CartItemDto(dto, count);
                });
    }

    @Transactional(readOnly = true)
    public CartItemDto getItem(Long id) {
        log.info("Получаем товар c id {}", id);
        return itemRepo.findById(id)
                .map(itemMapper::toDto)
                .map(itemDto -> {
                    Map<Long, Integer> cartItemCount = cartService.getCartItemCount(List.of(id));
                    Integer count = cartItemCount.get(id);
                    return new CartItemDto(itemDto, count);
                })
                .orElseThrow();
    }
}
