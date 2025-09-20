package ru.yandex.my.market.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.my.market.model.entity.CartItemEnt;

import java.util.List;

@Repository
public interface CartItemRepository extends CrudRepository<CartItemEnt, Long> {

    List<CartItemEnt> findAllByItemIdIn(List<Long> itemIds);
}
