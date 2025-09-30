package ru.yandex.my.market.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.enums.CartItemAction;
import ru.yandex.my.market.service.CartItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartItemService cartItemService;

    @Test
    void testRedirectRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));
    }

    @Test
    void testGetItems() throws Exception {
        CartItemDto mockItem = CartItemDto.MOCK;
        Page<CartItemDto> page = new PageImpl<>(List.of(mockItem));

        Mockito.when(cartItemService.getItems(anyString(), any(PageRequest.class))).thenReturn(page);

        ResultActions result = mockMvc.perform(get("/items")
                .param("search", "test")
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .param("sort", "NO"));

        result.andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attributeExists("sort"))
                .andExpect(model().attributeExists("items"));
    }

    @Test
    void testUpdateCartItemCountFromItemsView() throws Exception {
        ResultActions result = mockMvc.perform(post("/items")
                .param("id", "1")
                .param("action", CartItemAction.PLUS.name())
                .param("search", "")
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .param("sort", "NO"));

        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/items**"));

        Mockito.verify(cartItemService).updateCartItemCount(1L, CartItemAction.PLUS);
    }

    @Test
    void testGetItem() throws Exception {
        CartItemDto mockItem = CartItemDto.MOCK;

        Mockito.when(cartItemService.getItem(1L)).thenReturn(mockItem);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", mockItem));
    }

    @Test
    void testUpdateCartItemCountFromItemView() throws Exception {
        mockMvc.perform(post("/items/1")
                        .param("action", CartItemAction.MINUS.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/1"));

        Mockito.verify(cartItemService).updateCartItemCount(1L, CartItemAction.MINUS);
    }
}
