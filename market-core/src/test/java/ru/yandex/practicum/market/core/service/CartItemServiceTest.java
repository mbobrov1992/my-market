package ru.yandex.practicum.market.core.service;

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
import ru.yandex.practicum.market.core.model.entity.CartItemEnt;
import ru.yandex.practicum.market.core.model.entity.ItemEnt;
import ru.yandex.practicum.market.core.model.enums.CartItemAction;
import ru.yandex.practicum.market.core.repository.CartItemRepository;
import ru.yandex.practicum.market.core.repository.ItemCacheRepository;
import ru.yandex.practicum.market.core.repository.ItemRepository;

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

    private CartItemService cartItemService;

    @BeforeEach
    void setUp() {
        cartItemService = new CartItemService(cartItemRepo, itemRepo, itemCacheRepo, itemMapper, priceService, paymentService);
    }

    @Test
    void testGetItems() {
        ItemEnt item = new ItemEnt();
        item.setTitle("Тестовый товар");
        item.setDescription("Описание");
        item.setPrice(BigDecimal.ONE);
        itemRepo.save(item).block();

        ItemDto dto = new ItemDto(1, null, null, null, null);
        Mockito.when(itemMapper.toDto(any(ItemEnt.class))).thenReturn(dto);

        Page<CartItemDto> resultPage = cartItemService.getItems("Тестовый", PageRequest.of(0, 10)).block();

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).isNotEmpty();
        assertThat(resultPage.getContent().getFirst().count()).isEqualTo(0);
    }

    @Test
    void testGetItem_withCachedItem() {
        ItemEnt item = new ItemEnt();
        item.setTitle("Тестовый товар");
        item.setDescription("Описание");
        item.setPrice(BigDecimal.ONE);
        item = itemRepo.save(item).block();
        assertThat(item).isNotNull();

        Mockito.when(itemCacheRepo.findById(item.getId())).thenReturn(Mono.just(item));

        ItemDto dto = new ItemDto(1, null, null, null, null);
        Mockito.when(itemMapper.toDto(any(ItemEnt.class))).thenReturn(dto);

        CartItemDto result = cartItemService.getItem(item.getId()).block();
        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(0);

        Mockito.verify(itemCacheRepo, times(1))
                .findById(item.getId());
        Mockito.verify(itemCacheRepo, times(0))
                .save(item);
    }

    @Test
    void testGetItem_withNonCachedItem() {
        ItemEnt item = new ItemEnt();
        item.setTitle("Тестовый товар");
        item.setDescription("Описание");
        item.setPrice(BigDecimal.ONE);
        item = itemRepo.save(item).block();
        assertThat(item).isNotNull();

        Mockito.when(itemCacheRepo.findById(item.getId())).thenReturn(Mono.empty());
        Mockito.when(itemCacheRepo.save(any(ItemEnt.class))).thenReturn(Mono.just(item));

        ItemDto dto = new ItemDto(1, null, null, null, null);
        Mockito.when(itemMapper.toDto(any(ItemEnt.class))).thenReturn(dto);

        CartItemDto result = cartItemService.getItem(item.getId()).block();
        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(0);

        Mockito.verify(itemCacheRepo, times(1))
                .findById(item.getId());
        Mockito.verify(itemCacheRepo, times(1))
                .save(any(ItemEnt.class));
    }

    @Test
    void testGetCartItemCount_withItems_returnsCountIncludingZeroForMissing() {
        ItemEnt item1 = getMockItem();
        ItemEnt item2 = getMockItem();
        itemRepo.saveAll(List.of(item1, item2)).blockLast();

        CartItemEnt cartItem1 = new CartItemEnt();
        cartItem1.setItemId(item1.getId());
        cartItem1.setCount(3);
        cartItemRepo.save(cartItem1).block();

        List<Long> itemIds = List.of(item1.getId(), item2.getId());
        Map<Long, Integer> counts = cartItemService.getCartItemCount(itemIds).block();

        assertThat(counts).hasSize(2);
        assertThat(counts.get(item1.getId())).isEqualTo(3);
        assertThat(counts.get(item2.getId())).isEqualTo(0);
    }

    @Test
    void testUpdateCartItemCount_addNewItem() {
        ItemEnt item = getMockItem();
        itemRepo.save(item).block();

        cartItemService.updateCartItemCount(item.getId(), CartItemAction.PLUS).block();

        List<CartItemEnt> cartItems = cartItemRepo.findAll(Sort.by(Sort.Direction.ASC, "id")).collectList().block();
        assertThat(cartItems).hasSize(1);
        assertThat(cartItems.getFirst().getItemId()).isEqualTo(item.getId());
        assertThat(cartItems.getFirst().getCount()).isEqualTo(1);
    }

    @Test
    void testUpdateCartItemCount_deleteItem() {
        ItemEnt item = getMockItem();
        itemRepo.save(item).block();

        CartItemEnt cartItem = new CartItemEnt();
        cartItem.setItemId(item.getId());
        cartItem.setCount(2);
        cartItemRepo.save(cartItem);

        cartItemService.updateCartItemCount(item.getId(), CartItemAction.DELETE);
        assertThat(cartItemRepo.findByItemId(item.getId()).block()).isNull();
    }

    @Test
    void testDeleteCartItems() {
        ItemEnt item = getMockItem();
        itemRepo.save(item).block();

        CartItemEnt cartItem = new CartItemEnt();
        cartItem.setItemId(item.getId());
        cartItem.setCount(5);
        cartItemRepo.save(cartItem).block();

        cartItemService.deleteCartItems().block();

        assertThat(cartItemRepo.findAll().collectList().block()).isEmpty();
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
