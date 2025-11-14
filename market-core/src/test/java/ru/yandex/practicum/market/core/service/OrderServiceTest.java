package ru.yandex.practicum.market.core.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.AbstractIntegrationTest;
import ru.yandex.practicum.market.core.exception.OrderCreationException;
import ru.yandex.practicum.market.core.model.dto.OrderDto;
import ru.yandex.practicum.market.core.model.dto.PaymentResponse;
import ru.yandex.practicum.market.core.model.entity.*;
import ru.yandex.practicum.market.core.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class OrderServiceTest extends AbstractIntegrationTest {

    private static final String USER = "user";

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private CartRepository cartRepo;
    @Autowired
    private CartItemRepository cartItemRepo;

    @MockitoBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        UserEnt user = new UserEnt();
        user.setName(USER);
        user.setPassword("password");
        userRepo.save(user).block();

        CartEnt cart = new CartEnt();
        cart.setUserId(user.getId());
        cartRepo.save(cart).block();

        ItemEnt item1 = getMockItem();
        itemRepo.save(item1).block();

        CartItemEnt cartItem1 = new CartItemEnt();
        cartItem1.setCartId(cart.getId());
        cartItem1.setItemId(item1.getId());
        cartItem1.setCount(5);
        cartItemRepo.save(cartItem1).block();

        ItemEnt item2 = getMockItem();
        itemRepo.save(item2).block();

        CartItemEnt cartItem2 = new CartItemEnt();
        cartItem2.setCartId(cart.getId());
        cartItem2.setItemId(item2.getId());
        cartItem2.setCount(5);
        cartItemRepo.save(cartItem2).block();
    }

    @AfterEach
    void tearDown() {
        orderRepo.deleteAll().block();
        userRepo.deleteAll().block();
        itemRepo.deleteAll().block();
    }

    @Test
    void testCreateOrder() {
        when(paymentService.pay(any()))
                .thenReturn(Mono.just(new PaymentResponse()));

        Long orderId = orderService.createOrder(USER).block();

        assertThat(orderId).isNotNull();

        List<OrderEnt> orderEnts = orderRepo.findAll().collectList().block();
        assertThat(orderEnts).isNotEmpty();
        assertThat(orderEnts.getFirst().getId()).isEqualTo(orderId);

        assertThat(cartItemRepo.findAll().collectList().block()).isEmpty();
    }

    @Test
    void testCreateOrder_whenCartIsEmpty_shouldThrowException() {
        assertThatException()
                .isThrownBy(() -> orderService.createOrder("UNKNOWN_USER").block())
                .isExactlyInstanceOf(OrderCreationException.class);

        List<OrderEnt> orders = orderRepo.findAll().collectList().block();
        assertThat(orders).isEmpty();

        List<CartItemEnt> cartItems = cartItemRepo.findAll().collectList().block();
        assertThat(cartItems).isNotEmpty();

        verifyNoMoreInteractions(paymentService);
    }

    @Test
    void testGetOrders_andGetOrder() {
        when(paymentService.pay(any()))
                .thenReturn(Mono.just(new PaymentResponse()));

        Long orderId = orderService.createOrder(USER).block();

        List<OrderDto> orderDtos = orderService.getOrders(USER).collectList().block();
        assertThat(orderDtos).hasSize(1);
        assertThat(orderDtos.getFirst().id()).isEqualTo(orderId);

        OrderDto orderDto = orderService.getOrder(USER, orderId).block();
        assertThat(orderDto).isNotNull();
        assertThat(orderDto.id()).isEqualTo(orderId);
    }

    private ItemEnt getMockItem() {
        Random random = new Random();

        ItemEnt item = new ItemEnt();
        item.setTitle("Тестовый товар " + random.nextInt());
        item.setDescription("Описание товара " + random.nextInt());
        item.setPrice(BigDecimal.valueOf(1 + random.nextInt(10000)));
        item.setImagePath("http://random.test.server");
        return item;
    }
}
