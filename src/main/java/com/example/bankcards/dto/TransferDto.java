package com.example.bankcards.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для перевода денег между картами.
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferDto {
    /** ID карты отправителя */
    private UUID fromCardId;
    /** ID карты получателя */
    private UUID toCardId;
    /** Сумма перевода */
    private BigDecimal amount;
}
