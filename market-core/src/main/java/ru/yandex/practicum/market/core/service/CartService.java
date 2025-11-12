package ru.yandex.practicum.market.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.entity.CartEnt;
import ru.yandex.practicum.market.core.repository.CartRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartService {

    private final CartRepository cartRepo;
    private final UserService userService;

    public Mono<CartEnt> addCart(String username) {
        log.info("Создаем корзину пользователя: {}", username);

        return userService.getUser(username)
                .map(user -> {
                    CartEnt cart = new CartEnt();
                    cart.setUserId(user.getId());
                    return cart;
                })
                .flatMap(cartRepo::save);
    }

    public Mono<Long> getCartId(String username) {
        log.debug("Получаем идентификатор корзины пользователя: {}", username);

        return cartRepo.findCartIdByUsername(username);
    }
}
