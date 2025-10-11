package ru.yandex.my.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.my.market.exception.ItemNotFoundException;
import ru.yandex.my.market.mapper.ItemMapper;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.dto.ItemDto;
import ru.yandex.my.market.model.entity.CartItemEnt;
import ru.yandex.my.market.model.enums.CartItemAction;
import ru.yandex.my.market.repository.CartItemRepository;
import ru.yandex.my.market.repository.ItemRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.yandex.my.market.model.enums.CartItemAction.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartItemService {

    private final CartItemRepository cartItemRepo;
    private final ItemRepository itemRepo;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public Mono<Page<CartItemDto>> getItems(String search, Pageable pageable) {
        log.info("Получаем товары по строке: \"{}\"", search);

        return itemRepo.findAllByTitleIsContainingIgnoreCaseOrDescriptionIsContainingIgnoreCase(
                        search, search, pageable
                )
                .map(itemMapper::toDto)
                .collectList()
                .flatMap(itemDtos -> {
                    List<Long> itemIds = itemDtos.stream()
                            .map(ItemDto::id)
                            .toList();

                    return getCartItemCount(itemIds).map(map ->
                            itemDtos.stream()
                                    .map(itemDto -> new CartItemDto(itemDto, map.get(itemDto.id())))
                                    .toList());
                })
                .zipWith(itemRepo.countAllByTitleIsContainingIgnoreCaseOrDescriptionIsContainingIgnoreCase(search, search))
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Transactional(readOnly = true)
    public Mono<CartItemDto> getItem(Long id) {
        log.info("Получаем товар c id {}", id);

        return itemRepo.findById(id)
                .map(itemMapper::toDto)
                .flatMap(dto -> getCartItemCount(List.of(id)).map(map -> new CartItemDto(dto, map.get(id))))
                .switchIfEmpty(Mono.error(() -> new ItemNotFoundException(id)));
    }

    public Mono<Map<Long, Integer>> getCartItemCount(List<Long> itemIds) {
        if (itemIds.isEmpty()) return Mono.just(Map.of());

        log.info("Получаем товары с ids: {} в корзине", itemIds);

        return cartItemRepo.findAllByItemIdIn(itemIds)
                .collectMap(CartItemEnt::getItemId, CartItemEnt::getCount)
                .map(map -> {
                    itemIds.forEach(itemId -> map.putIfAbsent(itemId, 0));
                    return map;
                });
    }

    @Transactional
    public Mono<CartItemEnt> updateCartItemCount(Long itemId, CartItemAction action) {
        return cartItemRepo.findByItemId(itemId)
                .flatMap(cartItem -> updateOrDeleteCartItem(itemId, action, cartItem))
                .switchIfEmpty(Mono.defer(() -> {
                    if (action == PLUS) {
                        return addNewCartItem(itemId);
                    } else {
                        log.warn("Невозможно обработать действие {} для товара с id {}", action, itemId);
                        return Mono.empty();
                    }
                }));
    }

    private Mono<CartItemEnt> updateOrDeleteCartItem(Long itemId, CartItemAction action, CartItemEnt cartItem) {
        if (action == DELETE || cartItem.getCount() == 1 && action == MINUS) {
            log.info("Удаляем товар c id {} из корзины", itemId);
            return cartItemRepo.delete(cartItem)
                    .doOnSuccess(result -> log.info("Товар с id {} успешно удален из корзины", itemId))
                    .doOnError(ex -> log.error("Ошибка удаления товара с id {} из корзины: {}", itemId, ex.getMessage()))
                    .then(Mono.just(cartItem));
        } else {
            int delta = action == PLUS ? 1 : -1;
            log.info("Изменяем количество товара c id {} в корзине на дельту: {}", itemId, delta);
            return cartItemRepo.updateCartItemCount(itemId, delta)
                    .doOnSuccess(result -> log.info("Количество товара c id {} в корзине успешно изменено", itemId))
                    .doOnError(ex -> log.error("Ошибка изменения количества товара с id {} в корзине: {}", itemId, ex.getMessage()))
                    .then(Mono.just(cartItem));
        }
    }

    private Mono<CartItemEnt> addNewCartItem(Long itemId) {
        log.info("Добавляем товар c id {} в корзину", itemId);

        CartItemEnt cartItem = new CartItemEnt();
        cartItem.setItemId(itemId);
        cartItem.setCount(1);

        return cartItemRepo.save(cartItem)
                .doOnSuccess(result -> log.info("Товар c id {} успешно добавлен в корзину", itemId))
                .doOnError(ex -> log.error("Ошибка добавления товара с id {} в корзину: {}", itemId, ex.getMessage()));
    }

    @Transactional(readOnly = true)
    public Flux<CartItemDto> getCartItems() {
        log.info("Получаем товары из корзины");

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        return cartItemRepo.findAll(sort)
                .collectMap(CartItemEnt::getItemId, CartItemEnt::getCount)
                .flatMapMany(map -> {
                    Set<Long> itemIds = map.keySet();
                    return itemRepo.findAllById(itemIds)
                            .map(itemMapper::toDto)
                            .map(itemDto -> new CartItemDto(itemDto, map.get(itemDto.id())));
                });
    }

    public Mono<Void> deleteCartItems() {
        log.info("Удаляем товары из корзины");

        return cartItemRepo.deleteAll();
    }
}
