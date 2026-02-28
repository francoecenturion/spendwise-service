package com.spendwise.model.auth;

import com.spendwise.model.BaseEntity;
import com.spendwise.model.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "VERIFICATION_TOKEN")
@Data
@EqualsAndHashCode(callSuper = true)
public class VerificationToken extends BaseEntity {

    private String token;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    private LocalDateTime expiryDate;

}
