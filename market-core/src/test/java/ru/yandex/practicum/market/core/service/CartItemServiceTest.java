package ru.yandex.practicum.market.core.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.config.TestcontainersConfig;
import ru.yandex.practicum.market.core.mapper.ItemMapper;
import ru.yandex.practicum.market.core.model.dto.CartItemDto;
import ru.yandex.practicum.market.core.model.dto.ItemDto;
import ru.yandex.practicum.market.core.model.entity.CartEnt;
import ru.yandex.practicum.market.core.model.entity.CartItemEnt;
import ru.yandex.practicum.market.core.model.entity.ItemEnt;
import ru.yandex.practicum.market.core.model.entity.UserEnt;
import ru.yandex.practicum.market.core.model.enums.CartItemAction;
import ru.yandex.practicum.market.core.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@DataR2dbcTest
@Import(TestcontainersConfig.class)
public class CartItemServiceTest {

    private static final String USER = "user";

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private CartRepository cartRepo;
    @Autowired
    private CartItemRepository cartItemRepo;
    @Autowired
    private ItemRepository itemRepo;

    @MockitoBean
    private ItemCacheRepository itemCacheRepo;
    @MockitoBean
    private ItemMapper itemMapper;
    @MockitoBean
    private PriceService priceService;
    @MockitoBean
    private PaymentService paymentService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private CartService cartService;

    private CartItemService cartItemService;

    @BeforeEach
    void setUp() {
        cartItemService = new CartItemService(cartItemRepo, itemRepo, itemCacheRepo, itemMapper,
                priceService, paymentService, cartService, userService);
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll().block();
        itemRepo.deleteAll().block();
    }

    @Test
    void testGetItems() {
        ItemEnt item = itemRepo.save(getMockItem()).block();
        assertThat(item).isNotNull();

        ItemDto dto = new ItemDto(item.getId(), item.getTitle(), item.getImagePath(), item.getDescription(), item.getPrice());
        Mockito.when(itemMapper.toDto(any(ItemEnt.class))).thenReturn(dto);

        Mockito.when(cartService.getCartId(USER)).thenReturn(Mono.empty());

        Page<CartItemDto> resultPage = cartItemService.getItems(USER, "Тестовый", PageRequest.of(0, 10)).block();

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getContent().getFirst().id()).isEqualTo(item.getId());
        assertThat(resultPage.getContent().getFirst().count()).isEqualTo(0);

        Mockito.verify(cartService, times(1))
                .getCartId(USER);
    }

    @Test
    void testGetItem_withCachedItem() {
        ItemEnt item = itemRepo.save(getMockItem()).block();
        assertThat(item).isNotNull();

        Mockito.when(itemCacheRepo.findById(item.getId())).thenReturn(Mono.just(item));

        ItemDto dto = new ItemDto(item.getId(), item.getTitle(), item.getImagePath(), item.getDescription(), item.getPrice());
        Mockito.when(itemMapper.toDto(any(ItemEnt.class))).thenReturn(dto);

        Mockito.when(cartService.getCartId(USER)).thenReturn(Mono.empty());

        CartItemDto result = cartItemService.getItem(USER, item.getId()).block();
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(item.getId());
        assertThat(result.count()).isEqualTo(0);

        Mockito.verify(itemCacheRepo, times(1))
                .findById(item.getId());
        Mockito.verify(itemCacheRepo, times(0))
                .save(item);
        Mockito.verify(cartService, times(1))
                .getCartId(USER);
    }

    @Test
    void testGetItem_withNonCachedItem() {
        ItemEnt item = itemRepo.save(getMockItem()).block();
        assertThat(item).isNotNull();

        Mockito.when(itemCacheRepo.findById(item.getId())).thenReturn(Mono.empty());
        Mockito.when(itemCacheRepo.save(any(ItemEnt.class))).thenReturn(Mono.just(item));

        ItemDto dto = new ItemDto(item.getId(), item.getTitle(), item.getImagePath(), item.getDescription(), item.getPrice());
        Mockito.when(itemMapper.toDto(any(ItemEnt.class))).thenReturn(dto);

        Mockito.when(cartService.getCartId(USER)).thenReturn(Mono.empty());

        CartItemDto result = cartItemService.getItem(USER, item.getId()).block();
        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(0);

        Mockito.verify(itemCacheRepo, times(1))
                .findById(item.getId());
        Mockito.verify(itemCacheRepo, times(1))
                .save(any(ItemEnt.class));
        Mockito.verify(cartService, times(1))
                .getCartId(USER);
    }

    @Test
    void testGetCartItemCount_withItems_returnsCountIncludingZeroForMissing() {
        ItemEnt item1 = getMockItem();
        ItemEnt item2 = getMockItem();
        itemRepo.saveAll(List.of(item1, item2)).blockLast();

        long cartId = addMockCart().getId();

        CartItemEnt cartItem1 = new CartItemEnt();
        cartItem1.setCartId(cartId);
        cartItem1.setItemId(item1.getId());
        cartItem1.setCount(3);
        cartItemRepo.save(cartItem1).block();

        Mockito.when(cartService.getCartId(USER)).thenReturn(Mono.just(cartId));

        List<Long> itemIds = List.of(item1.getId(), item2.getId());
        Map<Long, Integer> counts = cartItemService.getCartItemCount(USER, itemIds).block();

        assertThat(counts).hasSize(2);
        assertThat(counts.get(item1.getId())).isEqualTo(3);
        assertThat(counts.get(item2.getId())).isEqualTo(0);

        Mockito.verify(cartService, times(1))
                .getCartId(USER);
    }

    @Test
    void testUpdateCartItemCount_addNewItem_cartExists() {
        ItemEnt item = getMockItem();
        itemRepo.save(item).block();

        long cartId = addMockCart().getId();

        Mockito.when(cartService.getCartId(USER)).thenReturn(Mono.just(cartId));

        cartItemService.updateCartItemCount(USER, item.getId(), CartItemAction.PLUS).block();

        List<CartItemEnt> cartItems = cartItemRepo.findAll(Sort.by(Sort.Direction.ASC, "id")).collectList().block();
        assertThat(cartItems).hasSize(1);
        assertThat(cartItems.getFirst().getCartId()).isEqualTo(cartId);
        assertThat(cartItems.getFirst().getItemId()).isEqualTo(item.getId());
        assertThat(cartItems.getFirst().getCount()).isEqualTo(1);

        Mockito.verify(cartService, times(1))
                .getCartId(USER);
        Mockito.verify(cartService, times(0))
                .addCart(any());
    }

    @Test
    void testUpdateCartItemCount_addNewItem_cartNotExists() {
        ItemEnt item = getMockItem();
        itemRepo.save(item).block();

        Mockito.when(cartService.getCartId(USER)).thenReturn(Mono.empty());

        CartEnt cart = addMockCart();
        Mockito.when(cartService.addCart(USER)).thenReturn(Mono.just(cart));

        cartItemService.updateCartItemCount(USER, item.getId(), CartItemAction.PLUS).block();

        List<CartItemEnt> cartItems = cartItemRepo.findAll(Sort.by(Sort.Direction.ASC, "id")).collectList().block();
        assertThat(cartItems).hasSize(1);
        assertThat(cartItems.getFirst().getCartId()).isEqualTo(cart.getId());
        assertThat(cartItems.getFirst().getItemId()).isEqualTo(item.getId());
        assertThat(cartItems.getFirst().getCount()).isEqualTo(1);

        Mockito.verify(cartService, times(1))
                .getCartId(USER);
        Mockito.verify(cartService, times(1))
                .addCart(USER);
    }

    @Test
    void testUpdateCartItemCount_deleteItem() {
        ItemEnt item = getMockItem();
        itemRepo.save(item).block();

        long cartId = addMockCart().getId();

        CartItemEnt cartItem = new CartItemEnt();
        cartItem.setCartId(cartId);
        cartItem.setItemId(item.getId());
        cartItem.setCount(2);
        cartItemRepo.save(cartItem).block();

        Mockito.when(cartService.getCartId(USER)).thenReturn(Mono.just(cartId));

        cartItemService.updateCartItemCount(USER, item.getId(), CartItemAction.DELETE).block();
        assertThat(cartItemRepo.findByCartIdAndItemId(cartId, item.getId()).block()).isNull();

        Mockito.verify(cartService, times(1))
                .getCartId(USER);
        Mockito.verify(cartService, times(0))
                .addCart(any());
    }

    @Test
    void testDeleteCartItems() {
        ItemEnt item = getMockItem();
        itemRepo.save(item).block();

        long cartId = addMockCart().getId();

        CartItemEnt cartItem = new CartItemEnt();
        cartItem.setCartId(cartId);
        cartItem.setItemId(item.getId());
        cartItem.setCount(5);
        cartItemRepo.save(cartItem).block();

        Mockito.when(cartService.getCartId(USER)).thenReturn(Mono.just(cartId));

        cartItemService.deleteCartItems(USER).block();

        assertThat(cartItemRepo.findAll().collectList().block()).isEmpty();

        Mockito.verify(cartService, times(1))
                .getCartId(USER);
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

    private CartEnt addMockCart() {
        CartEnt cart = new CartEnt();
        cart.setUserId(addMockUserId());
        cart = cartRepo.save(cart).block();

        assertThat(cart).isNotNull();

        return cart;
    }

    private Long addMockUserId() {
        UserEnt user = new UserEnt();
        user.setName(USER);
        user.setPassword("password");
        user = userRepo.save(user).block();

        assertThat(user).isNotNull();

        return user.getId();
    }
}
