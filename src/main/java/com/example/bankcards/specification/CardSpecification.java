package com.example.bankcards.specification;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CardSpecification {

    public static Specification<Card> hasMinBalance(BigDecimal minBalance) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("balance"), minBalance);
    }

    public static Specification<Card> hasMaxBalance(BigDecimal maxBalance) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("balance"), maxBalance);
    }

    public static Specification<Card> expiresBefore(LocalDateTime date) {
        return (root, query, cb) ->
                cb.lessThan(root.get("expireAt"), date);
    }

    public static Specification<Card> expiresAfter(LocalDateTime date) {
        return (root, query, cb) ->
                cb.greaterThan(root.get("expireAt"), date);
    }

    public static Specification<Card> isWaitingForBlock(Boolean isBlocked) {
        return (root, query, cb) ->
                cb.equal(root.get("blockFlag"), isBlocked);
    }

    public static Specification<Card> hasStatuses(List<CardStatus> statuses) {
        return (root, query, cb) ->
                root.get("status").in(statuses);
    }
}

