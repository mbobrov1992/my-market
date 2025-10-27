package ru.yandex.practicum.market.core.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.entity.ItemEnt;

import java.time.Duration;

@Slf4j
@Repository
public class ItemCacheRepository {

    private static final String CACHE_KEY = "item";

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final Duration timeToLive;

    public ItemCacheRepository(
            ReactiveRedisTemplate<String, Object> redisTemplate,
            @Value("${cache.item.ttl}") Duration timeToLive
    ) {
        this.redisTemplate = redisTemplate;
        this.timeToLive = timeToLive;
    }

    public Mono<ItemEnt> findById(Long id) {
        log.info("Получаем из кэша товар с id: {}", id);

        return redisTemplate.opsForValue()
                .get(buildCacheKey(id))
                .cast(ItemEnt.class);
    }

    public Mono<ItemEnt> save(ItemEnt item) {
        log.info("Сохраняем в кэш товар с id: {}", item.getId());

        return redisTemplate.opsForValue()
                .set(buildCacheKey(item.getId()), item, timeToLive)
                .thenReturn(item);
    }

    private String buildCacheKey(Long id) {
        return CACHE_KEY + ":" + id;
    }
}
