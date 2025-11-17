package ru.yandex.practicum.market.core.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.entity.CartEnt;

@Repository
public interface CartRepository extends R2dbcRepository<CartEnt, Long> {

    @Query("SELECT c.id FROM cart c JOIN users u ON c.user_id = u.id WHERE u.name = :username")
    Mono<Long> findCartIdByUsername(String username);
}
