package ru.yandex.practicum.market.core.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@Table("orders")
public class OrderEnt extends AuditableEntity {

    @Id
    @Column("id")
    private long id;

    @Column("total_price")
    private BigDecimal totalPrice;
}
