package ru.yandex.practicum.market.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.exception.CredentialsValidationException;
import ru.yandex.practicum.market.core.model.dto.SignUpForm;
import ru.yandex.practicum.market.core.service.UserService;

@RequiredArgsConstructor
@Controller
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public Mono<Rendering> login(ServerWebExchange exchange) {
        return Mono.just(Rendering.view("auth/login")
                .modelAttribute("logout", hasRequestParam(exchange, "logout"))
                .modelAttribute("error", hasRequestParam(exchange, "error"))
                .build());
    }

    private boolean hasRequestParam(ServerWebExchange exchange, String paramName) {
        return exchange.getRequest()
                .getQueryParams()
                .containsKey(paramName);
    }

    @GetMapping("/logout")
    public Mono<Rendering> logout() {
        return Mono.just(Rendering.view("auth/logout")
                .build());
    }

    @GetMapping("/signup")
    public Mono<Rendering> signup() {
        return Mono.just(Rendering.view("auth/signup")
                .build());
    }

    @PostMapping("/signup")
    public Mono<Rendering> signup(@ModelAttribute SignUpForm signUp) {
        return signUp.validate()
                .then(userService.addUser(signUp.username(), signUp.password()))
                .map(user -> Rendering.redirectTo("/").build())
                .onErrorResume(CredentialsValidationException.class, ex ->
                        Mono.just(Rendering.view("auth/signup")
                                .modelAttribute("error", ex.getMessage())
                                .build()));
    }
}
