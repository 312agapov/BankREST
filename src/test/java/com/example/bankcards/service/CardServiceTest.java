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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardService cardService;

    private static final int PAGINATION_LIMIT = 5;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(cardService, "paginationLimit", PAGINATION_LIMIT);
    }

    @Test
    void addCard_ValidData_ReturnsSavedCard() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        Card card = Card.builder()
                .cardNumber("1234567890123456")
                .expireAt(LocalDateTime.now().plusYears(2))
                .build();

        Card expectedCard = Card.builder()
                .id(UUID.randomUUID())
                .user(user)
                .cardNumber("1234567890123456")
                .expireAt(card.getExpireAt())
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .blockFlag(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenReturn(expectedCard);

        Card result = cardService.add(card, userId);

        assertNotNull(result.getId());
        assertEquals(user, result.getUser());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertFalse(result.getBlockFlag());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void activateCard_ExistingCard_ActivatesCard() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder()
                .id(cardId)
                .status(CardStatus.LOCKED)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        Card result = cardService.activateCard(cardId);

        assertEquals(CardStatus.ACTIVE, result.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void transferMoney_SufficientBalance_TransfersSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        TransferDto transfer = TransferDto.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(new BigDecimal("100.50"))
                .build();

        User user = User.builder().id(userId).build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(user)
                .balance(new BigDecimal("200.00"))
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .user(user)
                .balance(new BigDecimal("50.00"))
                .build();

        JwtAuthentication auth = new JwtAuthentication();
        auth.setUserId(userId);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(cardRepository.findByUserId(userId)).thenReturn(List.of(fromCard, toCard));
        when(cardRepository.saveAll(anyList())).thenReturn(List.of(fromCard, toCard));

        cardService.transferMoney(transfer);

        assertEquals(new BigDecimal("99.50"), fromCard.getBalance());
        assertEquals(new BigDecimal("150.50"), toCard.getBalance());
        verify(cardRepository).saveAll(List.of(fromCard, toCard));
    }

    @Test
    void findAllUserCards_WithPagination_ReturnsCorrectPage() {
        UUID userId = UUID.randomUUID();
        int page = 2;

        JwtAuthentication auth = new JwtAuthentication();
        auth.setUserId(userId);
        SecurityContextHolder.getContext().setAuthentication(auth);

        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            cards.add(Card.builder()
                    .id(UUID.randomUUID())
                    .user(User.builder().id(userId).build())
                    .build());
        }

        Page<Card> cardPage = new PageImpl<>(
                cards.subList(5, 10),
                PageRequest.of(1, PAGINATION_LIMIT),
                10
        );

        when(cardRepository.findByUserId(userId, PageRequest.of(page-1, PAGINATION_LIMIT)))
                .thenReturn(cardPage);

        DataDto<CardDto> result = cardService.findAllUserCards(page);

        assertEquals(5, result.getData().size());
        assertEquals(2, result.getTotalPages());
        assertEquals(10, result.getTotalElements());
    }

    @Test
    void findAllFilteredCards_WithMinBalanceFilter_ReturnsFilteredResults() {
        JwtAuthentication auth = mock(JwtAuthentication.class);
        when(auth.getUserId()).thenReturn(UUID.randomUUID());

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        User user = User.builder()
                .id(auth.getUserId())
                .username("testuser")
                .build();

        Card card1 = Card.builder()
                .id(UUID.randomUUID())
                .balance(new BigDecimal("150.00"))
                .user(user)
                .build();

        Card card2 = Card.builder()
                .id(UUID.randomUUID())
                .balance(new BigDecimal("200.00"))
                .user(user)
                .build();

        CardFilterRequest request = CardFilterRequest.builder()
                .minBalance(new BigDecimal("100"))
                .page(1)
                .build();

        Page<Card> cardPage = new PageImpl<>(
                List.of(card1, card2),
                PageRequest.of(0, PAGINATION_LIMIT),
                2
        );

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(cardPage);

        DataDto<CardDto> result = cardService.findAllFilteredCards(request);

        assertEquals(2, result.getData().size());
        assertTrue(result.getData().stream()
                .allMatch(dto -> dto.getBalance().compareTo(new BigDecimal("100")) >= 0));

        SecurityContextHolder.clearContext();
    }


    @Test
    void blockCardByUser_OwnActiveCard_SetsBlockFlagTrue() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        User user = User.builder().id(userId).build();
        Card card = Card.builder()
                .id(cardId)
                .user(user)
                .status(CardStatus.ACTIVE)
                .blockFlag(false)
                .build();

        JwtAuthentication auth = new JwtAuthentication();
        auth.setUserId(userId);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.blockCardByUser(cardId);

        assertTrue(card.getBlockFlag());
    }

    @Test
    void getBalance_OwnCard_ReturnsCorrectBalance() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        BigDecimal expectedBalance = new BigDecimal("1000.00");

        User user = User.builder().id(userId).build();
        Card card = Card.builder()
                .id(cardId)
                .user(user)
                .balance(expectedBalance)
                .build();

        JwtAuthentication auth = new JwtAuthentication();
        auth.setUserId(userId);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getBalance(cardId);

        assertEquals(expectedBalance, result);
    }

    @Test
    void getById_OwnCard_ReturnsCard() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        User user = User.builder().id(userId).build();
        Card expectedCard = Card.builder()
                .id(cardId)
                .user(user)
                .build();

        JwtAuthentication auth = new JwtAuthentication();
        auth.setUserId(userId);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(expectedCard));

        Card result = cardService.getById(cardId);

        assertEquals(expectedCard, result);
    }
}