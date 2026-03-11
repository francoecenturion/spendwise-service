package com.spendwise.model.auth;

import com.spendwise.enums.Role;
import com.spendwise.model.BaseEntity;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE")
    private Role role = Role.USER;

}
