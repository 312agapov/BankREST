package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardFilterRequest;
import com.example.bankcards.dto.DataDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.impl.JwtAuthentication;
import com.example.bankcards.specification.CardSpecification;
import com.example.bankcards.util.PageMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Value("${app.pagination.limit}")
    private int paginationLimit;

    public Card add(Card card, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("Пользователь с таким ID не найден!"));

        if (card.getCardNumber() == null || !card.getCardNumber().matches("\\d+")) {
            throw new IllegalArgumentException("Номер карты должен содержать только цифры.");
        }

        Card newCard = Card.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .cardNumber(card.getCardNumber())
                .blockFlag(false)
                .status(CardStatus.ACTIVE)
                .expireAt(card.getExpireAt())
                .build();

        return cardRepository.save(newCard);
    }

    public Card activateCard(UUID cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() ->
                new NoSuchElementException("Карта не была найдена в БД!"));
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    public Card blockCard(UUID cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() ->
                new NoSuchElementException("Карта не была найдена в БД!"));
        card.setStatus(CardStatus.LOCKED);
        return cardRepository.save(card);
    }

    public void blockCardByUser(UUID cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() ->
                new NoSuchElementException("Карта не была найдена в БД!"));

        JwtAuthentication auth = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();

        if (!card.getUser().getId().equals(auth.getUserId())){
            throw new AccessDeniedException("Нельзя смотреть чужие карточки!)");
        }

        if (card.getStatus().equals(CardStatus.LOCKED)){
            log.info("Карта уже имеет статус LOCKED!");
        } else if (card.getStatus().equals(CardStatus.EXPIRED)){
            log.info("Карта просрочена!");
        } else if (card.getStatus().equals(CardStatus.ACTIVE)){
            log.info("Карта будет заблокирована администратором!");
            card.setBlockFlag(true);
        }
    }

    @Transactional
    public void transferMoney(TransferDto transfer) {
        JwtAuthentication auth = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
        List<Card> userCards = cardRepository.findByUserId(auth.getUserId());

        Card fromCard = null;
        Card toCard = null;

        for (Card card : userCards) {
            if (fromCard == null && card.getId().equals(transfer.getFromCardId())) {
                fromCard = card;
            }
            if (toCard == null && card.getId().equals(transfer.getToCardId())) {
                toCard = card;
            }
            if (fromCard != null && toCard != null) break;
        }

        if (fromCard == null) throw new IllegalArgumentException("Карта отправителя не найдена");

        if (toCard == null) throw new IllegalArgumentException("Карта получателя не найдена");

        if (fromCard.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Баланс карты отправителя отрицательный");
        }

        BigDecimal newBalance = fromCard.getBalance().subtract(transfer.getAmount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Недостаточно средств");
        }

        fromCard.setBalance(newBalance);
        toCard.setBalance(toCard.getBalance().add(transfer.getAmount()));

        cardRepository.saveAll(List.of(fromCard, toCard));
    }

    public BigDecimal getBalance(UUID cardId){
        Card card = cardRepository.findById(cardId).orElseThrow(() ->
                new NoSuchElementException("Карта не была найдена в БД!"));

        JwtAuthentication auth = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();

        if (!card.getUser().getId().equals(auth.getUserId())){
            throw new AccessDeniedException("Нельзя смотреть чужие карточки!)");
        }
        return card.getBalance();
    }

    public Card getById(UUID id) {
        Card card = cardRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Карта не была найдена в БД!"));

        JwtAuthentication auth = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (!card.getUser().getId().equals(auth.getUserId())){
            throw new AccessDeniedException("Нельзя смотреть чужие карточки!)");
        }
        return card;
    }

    @Transactional
    public Card edit(Card updatedCard) {
        Card existingCard = cardRepository.findById(updatedCard.getId())
                .orElseThrow(() -> new NoSuchElementException("Карта не найдена"));

        if (updatedCard.getCardNumber() != null) {
            existingCard.setCardNumber(updatedCard.getCardNumber());
        }
        if (updatedCard.getExpireAt() != null) {
            existingCard.setExpireAt(updatedCard.getExpireAt());
        }
        if (updatedCard.getStatus() != null) {
            existingCard.setStatus(updatedCard.getStatus());
        }
        if (updatedCard.getBalance() != null) {
            existingCard.setBalance(updatedCard.getBalance());
        }
        if (updatedCard.getBlockFlag() != null) {
            existingCard.setBlockFlag(updatedCard.getBlockFlag());
        }

        return cardRepository.save(existingCard);
    }

    public void deleteById(UUID id) {
        cardRepository.deleteById(id);
    }

    public DataDto<Card> findAllCards(Integer page) {
        if (page != null) {
            int actualPage = page - 1;
            return PageMapper.mapToDataDto(
                    cardRepository.findAll(PageRequest.of(actualPage, paginationLimit))
            );
        }

        List<Card> cards = cardRepository.findAll();
        return DataDto.<Card>builder()
                .data(cards)
                .totalPages(1)
                .totalElements(cards.size())
                .build();
    }

    public DataDto<CardDto> findAllUserCards(Integer page) {

        JwtAuthentication auth = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
        UUID userId = auth.getUserId();

        if (page != null) {
            int actualPage = page - 1;

            Page<Card> cardPage = cardRepository.findByUserId(userId, PageRequest.of(actualPage, paginationLimit));
            Page<CardDto> dtoPage = cardPage.map(card -> CardDto.fromEntity(card));
            return PageMapper.mapToDataDto(dtoPage);
        }

        List<Card> cards = cardRepository.findByUserId(userId);

        List<CardDto> cardDtos = cards.stream()
                .map(card -> CardDto.fromEntity(card))
                .toList();

        return DataDto.<CardDto>builder()
                .data(cardDtos)
                .totalPages(1)
                .totalElements(cards.size())
                .build();
    }

    public DataDto<CardDto> findAllFilteredCards(CardFilterRequest request) {
        JwtAuthentication auth = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
        UUID userId = auth.getUserId();

        Specification<Card> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId)
        );

        if (request.getMinBalance() != null) {
            spec = spec.and(CardSpecification.hasMinBalance(request.getMinBalance()));
        }

        if (request.getMaxBalance() != null) {
            spec = spec.and(CardSpecification.hasMaxBalance(request.getMaxBalance()));
        }

        if (request.getExpiresBeforeDate() != null) {
            spec = spec.and(CardSpecification.expiresBefore(request.getExpiresBeforeDate()));
        }

        if (request.getExpiresAfterDate() != null) {
            spec = spec.and(CardSpecification.expiresAfter(request.getExpiresAfterDate()));
        }

        if (request.getIsWaitingForBlock() != null) {
            spec = spec.and(CardSpecification.isWaitingForBlock(request.getIsWaitingForBlock()));
        }

        if (request.getCardStatus() != null && !request.getCardStatus().isEmpty()) {
            spec = spec.and(CardSpecification.hasStatuses(request.getCardStatus()));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "expireAt");

        if (request.getPage() != null && request.getPage() > 0) {
            int actualPage = request.getPage() - 1;
            Pageable pageable = PageRequest.of(actualPage, paginationLimit, sort);
            Page<Card> pageResult = cardRepository.findAll(spec, pageable);

            List<CardDto> dtoList = pageResult.getContent().stream()
                    .map(CardDto::fromEntity)
                    .toList();

            return DataDto.<CardDto>builder()
                    .data(dtoList)
                    .totalPages(pageResult.getTotalPages())
                    .totalElements(pageResult.getTotalElements())
                    .build();
        } else {
            List<Card> cards = cardRepository.findAll(spec, sort);
            List<CardDto> dtoList = cards.stream()
                    .map(CardDto::fromEntity)
                    .toList();

            return DataDto.<CardDto>builder()
                    .data(dtoList)
                    .totalPages(1)
                    .totalElements(cards.size())
                    .build();
        }
    }
}
