package ru.yandex.my.market.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.enums.CartItemAction;
import ru.yandex.my.market.service.CartItemService;
import ru.yandex.my.market.service.PriceService;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartItemService cartItemService;

    @MockitoBean
    private PriceService priceService;

    @Test
    void testGetCartItems() throws Exception {
        CartItemDto mockItem = CartItemDto.MOCK;
        List<CartItemDto> mockItems = List.of(mockItem);
        BigDecimal mockTotalPrice = BigDecimal.valueOf(100);

        Mockito.when(cartItemService.getCartItems()).thenReturn(mockItems);
        Mockito.when(priceService.calculatePrice(mockItems)).thenReturn(mockTotalPrice);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", mockItems))
                .andExpect(model().attribute("total", mockTotalPrice));
    }

    @Test
    void testUpdateCartItemCountFromCartView() throws Exception {
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", CartItemAction.MINUS.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        Mockito.verify(cartItemService).updateCartItemCount(1L, CartItemAction.MINUS);
    }
}
