package ru.yandex.practicum.market.core.service;

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
import ru.yandex.practicum.market.core.exception.ItemNotFoundException;
import ru.yandex.practicum.market.core.mapper.ItemMapper;
import ru.yandex.practicum.market.core.model.dto.PaymentInfo;
import ru.yandex.practicum.market.core.model.dto.CartItemDto;
import ru.yandex.practicum.market.core.model.dto.CartViewDto;
import ru.yandex.practicum.market.core.model.dto.ItemDto;
import ru.yandex.practicum.market.core.model.entity.CartEnt;
import ru.yandex.practicum.market.core.model.entity.CartItemEnt;
import ru.yandex.practicum.market.core.model.enums.CartItemAction;
import ru.yandex.practicum.market.core.repository.CartItemRepository;
import ru.yandex.practicum.market.core.repository.ItemCacheRepository;
import ru.yandex.practicum.market.core.repository.ItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.yandex.practicum.market.core.model.enums.CartItemAction.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartItemService {

    private final CartItemRepository cartItemRepo;
    private final ItemRepository itemRepo;
    private final ItemCacheRepository itemCacheRepo;
    private final ItemMapper itemMapper;
    private final PriceService priceService;
    private final PaymentService paymentService;
    private final CartService cartService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Mono<Page<CartItemDto>> getItems(String username, String search, Pageable pageable) {
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

                    return getCartItemCount(username, itemIds).map(map ->
                            itemDtos.stream()
                                    .map(itemDto -> new CartItemDto(itemDto, map.getOrDefault(itemDto.id(), 0)))
                                    .toList());
                })
                .zipWith(itemRepo.countAllByTitleIsContainingIgnoreCaseOrDescriptionIsContainingIgnoreCase(search, search))
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Transactional(readOnly = true)
    public Mono<CartItemDto> getItem(String username, Long id) {
        log.info("Получаем товар c id {}", id);

        return itemCacheRepo.findById(id)
                .switchIfEmpty(itemRepo.findById(id)
                        .flatMap(itemCacheRepo::save))
                .map(itemMapper::toDto)
                .flatMap(dto -> getCartItemCount(username, List.of(id))
                        .map(map -> new CartItemDto(dto, map.getOrDefault(id, 0))))
                .switchIfEmpty(Mono.error(() -> new ItemNotFoundException(id)));
    }

    public Mono<Map<Long, Integer>> getCartItemCount(String username, List<Long> itemIds) {
        if (itemIds.isEmpty()) return Mono.just(Map.of());

        log.info("Получаем товары с ids: {} в корзине пользователя: {}", itemIds, username);

        return cartService.getCartId(username)
                .flatMap(cartId -> cartItemRepo.findAllByCartIdAndItemIdIn(cartId, itemIds)
                        .collectMap(CartItemEnt::getItemId, CartItemEnt::getCount)
                        .map(map -> {
                            itemIds.forEach(itemId -> map.putIfAbsent(itemId, 0));
                            return map;
                        }))
                .switchIfEmpty(Mono.just(Map.of()));
    }

    @Transactional
    public Mono<CartItemEnt> updateCartItemCount(String username, Long itemId, CartItemAction action) {
        return getOrCreateCartId(username, action)
                .flatMap(cartId -> cartItemRepo.findByCartIdAndItemId(cartId, itemId)
                        .flatMap(cartItem -> updateOrDeleteCartItem(cartId, itemId, action, cartItem))
                        .switchIfEmpty(Mono.defer(() -> {
                            if (action == PLUS) {
                                return addNewCartItem(cartId, itemId);
                            } else {
                                log.warn("Невозможно обработать действие {} для товара с id {}", action, itemId);
                                return Mono.empty();
                            }
                        })));
    }

    private Mono<Long> getOrCreateCartId(String username, CartItemAction action) {
        return cartService.getCartId(username)
                .switchIfEmpty(Mono.defer(() -> {
                    if (action == PLUS) {
                        return cartService.addCart(username)
                                .map(CartEnt::getId);
                    }
                    return Mono.empty();
                }));
    }

    private Mono<CartItemEnt> updateOrDeleteCartItem(Long cartId, Long itemId, CartItemAction action, CartItemEnt cartItem) {
        if (action == DELETE || cartItem.getCount() == 1 && action == MINUS) {
            log.info("Удаляем товар c id {} из корзины", itemId);
            return cartItemRepo.delete(cartItem)
                    .doOnSuccess(result -> log.info("Товар с id {} успешно удален из корзины", itemId))
                    .doOnError(ex -> log.error("Ошибка удаления товара с id {} из корзины: {}", itemId, ex.getMessage()))
                    .then(Mono.just(cartItem));
        } else {
            int delta = action == PLUS ? 1 : -1;
            log.info("Изменяем количество товара c id {} в корзине на дельту: {}", itemId, delta);
            return cartItemRepo.updateCartItemCount(cartId, itemId, delta)
                    .doOnSuccess(result -> log.info("Количество товара c id {} в корзине успешно изменено", itemId))
                    .doOnError(ex -> log.error("Ошибка изменения количества товара с id {} в корзине: {}", itemId, ex.getMessage()))
                    .then(Mono.just(cartItem));
        }
    }

    private Mono<CartItemEnt> addNewCartItem(Long cartId, Long itemId) {
        log.info("Добавляем товар c id {} в корзину", itemId);

        CartItemEnt cartItem = new CartItemEnt();
        cartItem.setCartId(cartId);
        cartItem.setItemId(itemId);
        cartItem.setCount(1);

        return cartItemRepo.save(cartItem)
                .doOnSuccess(result -> log.info("Товар c id {} успешно добавлен в корзину", itemId))
                .doOnError(ex -> log.error("Ошибка добавления товара с id {} в корзину: {}", itemId, ex.getMessage()));
    }

    @Transactional(readOnly = true)
    public Mono<CartViewDto> getCartView(String username) {
        return getCartItems(username)
                .collectList()
                .flatMap(cartItems -> {
                    BigDecimal totalPrice = priceService.calculatePrice(cartItems);
                    return userService.getUser(username)
                            .flatMap(user -> paymentService.getBalance(String.valueOf(user.getId()))
                                    .map(response -> new CartViewDto(
                                            cartItems,
                                            totalPrice,
                                            new PaymentInfo(response.getBalance(), true)
                                    ))
                                    .onErrorReturn(
                                            new CartViewDto(
                                                    cartItems,
                                                    totalPrice,
                                                    new PaymentInfo(null, false))
                                    ));
                });
    }

    @Transactional(readOnly = true)
    public Flux<CartItemDto> getCartItems(String username) {
        log.info("Получаем товары из корзины пользователя: {}", username);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        return cartService.getCartId(username)
                .flatMapMany(cartId -> cartItemRepo.findAllByCartId(cartId, sort)
                        .collectMap(CartItemEnt::getItemId, CartItemEnt::getCount)
                        .flatMapMany(map -> {
                            Set<Long> itemIds = map.keySet();
                            return itemRepo.findAllById(itemIds)
                                    .map(itemMapper::toDto)
                                    .map(itemDto -> new CartItemDto(itemDto, map.get(itemDto.id())));
                        }));
    }

    public Mono<Void> deleteCartItems(String username) {
        log.info("Удаляем товары из корзины пользователя: {}", username);

        return cartService.getCartId(username)
                .flatMap(cartItemRepo::deleteAllByCartId);
    }
}
