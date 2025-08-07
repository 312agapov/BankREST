package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Параметры фильтрации карт.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardFilterRequest {
    /** Минимальный баланс */
    private BigDecimal minBalance;
    /** Максимальный баланс */
    private BigDecimal maxBalance;
    /** Действует до (дата) */
    private LocalDateTime expiresBeforeDate;
    /** Действует после (дата) */
    private LocalDateTime expiresAfterDate;
    /** Ожидает блокировки */
    private Boolean isWaitingForBlock;
    /** Статусы карт */
    private List<CardStatus> cardStatus;
    /** Номер страницы */
    private Integer page;
}
