package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для карты (без полного номера).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    /** ID карты */
    private UUID id;
    /** Маскированный номер (****1234) */
    private String maskedCardNumber;
    /** ID владельца */
    private UUID userId;
    /** Срок действия */
    private LocalDateTime expireAt;
    /** Статус */
    private CardStatus status;
    /** Баланс */
    private BigDecimal balance;
    /** Заблокирована */
    private Boolean blockFlag;

    /**
     * Создает DTO из сущности Card.
     * @param card сущность карты
     * @return DTO карты
     */
    public static CardDto fromEntity(Card card) {
        return CardDto.builder()
                .id(card.getId())
                .maskedCardNumber(maskCardNumber(card.getCardNumber()))
                .userId(card.getUser().getId())
                .expireAt(card.getExpireAt())
                .status(card.getStatus())
                .balance(card.getBalance())
                .blockFlag(card.getBlockFlag())
                .build();
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}
