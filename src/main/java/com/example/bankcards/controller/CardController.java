package com.example.bankcards.controller;

import com.example.bankcards.dto.CardFilterRequest;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<?> addCard(@RequestBody Card card, @RequestParam UUID userId) {
        return ResponseEntity.ok(cardService.add(card, userId));
    }

    @PostMapping("/adminactivate")
    public ResponseEntity<?> activateCard(@RequestParam UUID cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }

    @PostMapping("/adminblock")
    public ResponseEntity<?> blockCard(@RequestParam UUID cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    @PostMapping("/userblock")
    public ResponseEntity<?> blockCardByUser(@RequestParam UUID cardId) {
        cardService.blockCardByUser(cardId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(@RequestBody TransferDto transfer) {
        cardService.transferMoney(transfer);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/sort")
    public ResponseEntity<?> getCardsSort(@RequestBody CardFilterRequest cfr) {
        return ResponseEntity.ok(cardService.findAllFilteredCards(cfr));
    }

    @GetMapping("/allcardsadmin")
    public ResponseEntity<?> getCards(@RequestParam(required = false) Integer page) {
        return ResponseEntity.ok(cardService.findAllCards(page));
    }

    @GetMapping
    public ResponseEntity<?> getUsersCards(@RequestParam(required = false) Integer page) {
        return ResponseEntity.ok(cardService.findAllUserCards(page));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCardById(@PathVariable("cardId") UUID cardId) {
        return ResponseEntity.ok(cardService.getById(cardId));
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getCardBalance(@RequestParam UUID cardId) {
        return ResponseEntity.ok(cardService.getBalance(cardId));
    }

    @PutMapping
    public ResponseEntity<?> editCard(@RequestBody Card card) {
        return ResponseEntity.ok(cardService.edit(card));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteCard(@PathVariable("cardId") UUID cardId) {
        cardService.deleteById(cardId);
        return ResponseEntity.ok().build();
    }
}
