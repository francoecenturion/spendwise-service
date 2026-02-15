package com.spendwise.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "LAST_UPDATE_DATE")
    private LocalDateTime lastUpdateDate;

    @PrePersist
    protected void prePersist() {
        this.creationDate = LocalDateTime.now();
        this.preUpdate();
    }

    @PreUpdate
    protected void preUpdate() {
        this.lastUpdateDate = LocalDateTime.now();
    }

}
