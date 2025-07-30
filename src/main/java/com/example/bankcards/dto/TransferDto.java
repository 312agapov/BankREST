package com.example.bankcards.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferDto {
    private UUID fromCardId;
    private UUID toCardId;
    private BigDecimal amount;
}
