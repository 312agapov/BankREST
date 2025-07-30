package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private UUID id;
    private String maskedCardNumber; // Будет содержать только последние 4 цифры
    private UUID userId;
    private LocalDateTime expireAt;
    private CardStatus status;
    private BigDecimal balance;
    private Boolean blockFlag;


    public static CardDto fromEntity(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedCardNumber(maskCardNumber(card.getCardNumber()));
        dto.setUserId(card.getUser().getId());
        dto.setExpireAt(card.getExpireAt());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setBlockFlag(card.getBlockFlag());
        return dto;
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber; // или можно выбросить исключение
        }

        String masked = cardNumber.substring(0, cardNumber.length() - 4)
                .replaceAll(".", "*");
        String lastFour = cardNumber.substring(cardNumber.length() - 4);

        return masked + lastFour;
    }
}
