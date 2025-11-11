package ru.yandex.practicum.market.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.exception.CredentialsValidationException;
import ru.yandex.practicum.market.core.model.entity.UserEnt;
import ru.yandex.practicum.market.core.repository.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserEnt> addUser(String username, String password) {
        log.info("Создаем пользователя: {}", username);

        return getUser(username)
                .flatMap(exists -> Mono.<UserEnt>error(
                        new CredentialsValidationException("Пользователь уже существует"))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    UserEnt user = new UserEnt();
                    user.setName(username);
                    user.setPassword(passwordEncoder.encode(password));
                    return userRepo.save(user);
                }));
    }

    public Mono<UserEnt> getUser(String username) {
        log.debug("Получаем пользователя: {}", username);

        return userRepo.findByName(username);
    }
}
