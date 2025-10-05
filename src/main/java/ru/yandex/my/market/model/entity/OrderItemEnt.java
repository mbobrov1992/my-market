package ru.yandex.my.market.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@Table("order_item")
public class OrderItemEnt extends AuditableEntity {

    @Id
    @Column("id")
    private long id;

    @Column("order_id")
    private long orderId;

    @Column("item_id")
    private long itemId;

    @Column("count")
    private Integer count;

    @Column("price")
    private BigDecimal price;
}
