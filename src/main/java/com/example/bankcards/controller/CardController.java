package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardFilterRequest;
import com.example.bankcards.dto.DataDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Контроллер для управления банковскими картами.
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    /**
     * Создает новую карту для пользователя.
     * @param card данные карты
     * @param userId ID пользователя
     * @return созданная карта
     */
    @PostMapping
    public ResponseEntity<Card> addCard(@RequestBody Card card, @RequestParam UUID userId) {
        return ResponseEntity.ok(cardService.add(card, userId));
    }

    /**
     * Активирует карту (админ).
     * @param cardId ID карты
     * @return обновленная карта
     */
    @PostMapping("/adminactivate")
    public ResponseEntity<Card> activateCard(@RequestParam UUID cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }

    /**
     * Блокирует карту (админ).
     * @param cardId ID карты
     * @return обновленная карта
     */
    @PostMapping("/adminblock")
    public ResponseEntity<Card> blockCard(@RequestParam UUID cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    /**
     * Блокирует карту (пользователь).
     * @param cardId ID карты
     */
    @PostMapping("/userblock")
    public ResponseEntity<Void> blockCardByUser(@RequestParam UUID cardId) {
        cardService.blockCardByUser(cardId);
        return ResponseEntity.ok().build();
    }

    /**
     * Переводит деньги между картами.
     * @param transfer DTO перевода
     */
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferMoney(@RequestBody TransferDto transfer) {
        cardService.transferMoney(transfer);
        return ResponseEntity.ok().build();
    }

    /**
     * Возвращает отфильтрованный список карт.
     * @param cfr параметры фильтрации
     * @return пагинированный список карт
     */
    @PostMapping(path = "/sort")
    public ResponseEntity<DataDto<CardDto>> getCardsSort(@RequestBody CardFilterRequest cfr) {
        return ResponseEntity.ok(cardService.findAllFilteredCards(cfr));
    }

    /**
     * Возвращает все карты (админ).
     * @param page номер страницы
     * @return пагинированный список карт
     */
    @GetMapping("/allcardsadmin")
    public ResponseEntity<DataDto<Card>> getCards(@RequestParam(required = false) Integer page) {
        return ResponseEntity.ok(cardService.findAllCards(page));
    }

    /**
     * Возвращает карты текущего пользователя.
     * @param page номер страницы
     * @return пагинированный список карт
     */
    @GetMapping
    public ResponseEntity<DataDto<CardDto>> getUsersCards(@RequestParam(required = false) Integer page) {
        return ResponseEntity.ok(cardService.findAllUserCards(page));
    }

    /**
     * Возвращает карту по ID.
     * @param cardId ID карты
     * @return данные карты
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<Card> getCardById(@PathVariable("cardId") UUID cardId) {
        return ResponseEntity.ok(cardService.getById(cardId));
    }

    /**
     * Возвращает баланс карты.
     * @param cardId ID карты
     * @return текущий баланс
     */
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getCardBalance(@RequestParam UUID cardId) {
        return ResponseEntity.ok(cardService.getBalance(cardId));
    }

    /**
     * Редактирует данные карты.
     * @param card новые данные карты
     * @return обновленная карта
     */
    @PutMapping
    public ResponseEntity<Card> editCard(@RequestBody Card card) {
        return ResponseEntity.ok(cardService.edit(card));
    }

    /**
     * Удаляет карту.
     * @param cardId ID карты
     */
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable("cardId") UUID cardId) {
        cardService.deleteById(cardId);
        return ResponseEntity.ok().build();
    }
}
