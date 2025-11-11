package ru.yandex.practicum.market.core.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.entity.UserEnt;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserEnt, Long> {

    Mono<UserEnt> findByName(String name);
}
