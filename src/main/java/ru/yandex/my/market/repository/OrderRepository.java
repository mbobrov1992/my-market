package ru.yandex.my.market.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.my.market.model.entity.OrderEnt;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEnt, Long> {

    @EntityGraph(attributePaths = {"items.item"})
    List<OrderEnt> findAll(Sort sort);

    @EntityGraph(attributePaths = {"items.item"})
    Optional<OrderEnt> findById(Long id);
}
