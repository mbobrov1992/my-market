package ru.yandex.practicum.market.core.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("users")
public class UserEnt extends AuditableEntity {

    @Id
    @Column("id")
    private long id;

    @Column("name")
    private String name;

    @Column("password")
    private String password;
}
