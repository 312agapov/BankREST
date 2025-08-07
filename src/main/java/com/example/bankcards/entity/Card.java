package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String cardNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expireAt;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private BigDecimal balance;

    private Boolean blockFlag = false;

}
