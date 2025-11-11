package ru.yandex.practicum.market.core.model.dto;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.exception.CredentialsValidationException;

public record SignUpForm(
        String username,
        String password,
        String passwordConfirm
) {
    public Mono<Void> validate() {
        if (username == null || username.isEmpty()) {
            return Mono.error(new CredentialsValidationException("Не указан логин"));
        }
        if (username.isBlank()) {
            return Mono.error(new CredentialsValidationException("Логин не может быть пустым"));
        }
        if (password == null || password.isEmpty()) {
            return Mono.error(new CredentialsValidationException("Не указан пароль"));
        }
        if (password.isBlank()) {
            return Mono.error(new CredentialsValidationException("Пароль не может быть пустым"));
        }
        if (!password.equals(passwordConfirm)) {
            return Mono.error(new CredentialsValidationException("Пароли не совпадают"));
        }
        return Mono.empty();
    }
}
