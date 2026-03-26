package com.spendwise.model.auth;

import com.spendwise.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "PASSWORD_RESET_TOKEN")
@Data
@EqualsAndHashCode(callSuper = true)
public class PasswordResetToken extends BaseEntity {

    private String token;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    private LocalDateTime expiryDate;

}
