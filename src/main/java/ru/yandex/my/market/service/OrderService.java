package ru.yandex.my.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.my.market.exception.OrderCreationException;
import ru.yandex.my.market.exception.OrderNotFoundException;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.dto.OrderDto;
import ru.yandex.my.market.model.dto.OrderItemDto;
import ru.yandex.my.market.model.entity.ItemEnt;
import ru.yandex.my.market.model.entity.OrderEnt;
import ru.yandex.my.market.model.entity.OrderItemEnt;
import ru.yandex.my.market.repository.ItemRepository;
import ru.yandex.my.market.repository.OrderItemRepository;
import ru.yandex.my.market.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final ItemRepository itemRepo;
    private final CartItemService cartItemService;
    private final PriceService priceService;

    @Transactional(readOnly = true)
    public Flux<OrderDto> getOrders() {
        log.info("Получаем список заказов");

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        return orderRepo.findAll(sort)
                .collectList()
                .flatMapMany(this::toOrderDto);
    }

    @Transactional(readOnly = true)
    public Mono<OrderDto> getOrder(Long id) {
        log.info("Получаем заказ с id: {}", id);

        return orderRepo.findById(id)
                .flatMap(this::toOrderDto)
                .switchIfEmpty(Mono.error(() -> new OrderNotFoundException(id)));
    }

    private Mono<OrderDto> toOrderDto(OrderEnt order) {
        return toOrderDto(List.of(order))
                .next();
    }

    private Flux<OrderDto> toOrderDto(List<OrderEnt> orders) {
        Map<Long, OrderEnt> orderIdOrderMap = orders.stream()
                .collect(Collectors.toMap(OrderEnt::getId, Function.identity()));

        return getOrderItems(orderIdOrderMap.keySet()).flatMap(orderIdOrderItemsMap -> {
                    Set<Long> itemIds = orderIdOrderItemsMap.values().stream()
                            .flatMap(Collection::stream)
                            .map(OrderItemEnt::getItemId)
                            .collect(Collectors.toSet());

                    return getItems(itemIds).map(itemIdItemMap -> {
                        List<OrderDto> orderDtos = new ArrayList<>();

                        orderIdOrderItemsMap.forEach((orderId, orderItems) -> {
                            List<OrderItemDto> orderItemDtos = orderItems.stream()
                                    .map(orderItem -> new OrderItemDto(
                                            itemIdItemMap.get(orderItem.getItemId()).getTitle(),
                                            orderItem.getCount(),
                                            orderItem.getPrice()
                                    ))
                                    .toList();

                            OrderDto orderDto = new OrderDto(
                                    orderId,
                                    orderItemDtos,
                                    orderIdOrderMap.get(orderId).getTotalPrice()
                            );

                            orderDtos.add(orderDto);
                        });

                        return orderDtos;
                    });
                })
                .flatMapIterable(list -> list);
    }

    private Mono<Map<Long, Collection<OrderItemEnt>>> getOrderItems(Collection<Long> orderIds) {
        log.info("Получаем товары заказов с ids: {}", orderIds);

        return orderItemRepo.findAllByOrderIdIn(orderIds)
                .collectMultimap(OrderItemEnt::getOrderId, orderItem -> orderItem);
    }

    private Mono<Map<Long, ItemEnt>> getItems(Collection<Long> itemIds) {
        log.info("Получаем товары с ids: {}", itemIds);

        return itemRepo.findAllById(itemIds)
                .collectMap(ItemEnt::getId, item -> item);
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> createOrder() {
        log.info("Начинаем процесс создания заказа");

        return cartItemService.getCartItems()
                .collectList()
                .flatMap(cartItems -> cartItemService.deleteCartItems().thenReturn(cartItems))
                .flatMap(this::createOrder)
                .doOnSuccess(order -> log.info("Создан заказ с id: {}", order.getId()))
                .map(OrderEnt::getId);
    }

    private Mono<OrderEnt> createOrder(List<CartItemDto> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new OrderCreationException("Отсутствуют товары в корзине");
        }

        OrderEnt order = new OrderEnt();
        order.setTotalPrice(calculatePrice(cartItems));

        return orderRepo.save(order)
                .flatMap(orderEnt -> createOrderItems(orderEnt, cartItems)
                        .collectList()
                        .thenReturn(orderEnt));
    }

    private Flux<OrderItemEnt> createOrderItems(OrderEnt order, List<CartItemDto> cartItems) {
        List<OrderItemEnt> orderItems = new ArrayList<>();

        cartItems.forEach(cartItem -> {
            OrderItemEnt orderItem = createOrderItem(order, cartItem);
            orderItems.add(orderItem);
        });

        return orderItemRepo.saveAll(orderItems);
    }

    private OrderItemEnt createOrderItem(OrderEnt order, CartItemDto cartItem) {
        OrderItemEnt orderItem = new OrderItemEnt();
        orderItem.setOrderId(order.getId());
        orderItem.setItemId(cartItem.id());
        orderItem.setCount(cartItem.count());
        orderItem.setPrice(cartItem.price());
        return orderItem;
    }

    private BigDecimal calculatePrice(List<CartItemDto> cartItems) {
        return priceService.calculatePrice(cartItems);
    }
}
