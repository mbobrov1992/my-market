package ru.yandex.practicum.market.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepo;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepo.findByName(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден: " + username)))
                .map(user -> User.builder()
                        .username(user.getName())
                        .password(user.getPassword())
                        .build());
    }
}
