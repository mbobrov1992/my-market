package ru.yandex.my.market.model.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;

import java.time.LocalDateTime;

public abstract class AuditableEntity {

    @InsertOnlyProperty
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
