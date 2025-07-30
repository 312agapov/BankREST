package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardFilterRequest {
    private BigDecimal minBalance;
    private BigDecimal maxBalance;

    private LocalDateTime expiresBeforeDate;
    private LocalDateTime expiresAfterDate;

    private Boolean isWaitingForBlock;

    private List<CardStatus> cardStatus;

    private Integer page;
}
