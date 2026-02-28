package com.spendwise.model.user;

import com.spendwise.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "APP_USER")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Column(unique = true)
    private String email;
    private String passwordHash;
    private String name;
    private String surname;
    private Boolean enabled;
    private String profilePicture;

}
