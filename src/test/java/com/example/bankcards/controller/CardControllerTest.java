package com.example.bankcards.controller;

import com.example.bankcards.dto.CardFilterRequest;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @Test
    void addCard_ValidRequest_ReturnsOk() throws Exception {
        Card card = new Card();
        UUID userId = UUID.randomUUID();

        Mockito.when(cardService.add(any(Card.class), any(UUID.class))).thenReturn(card);

        mockMvc.perform(post("/api/cards?userId={userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card)))
                .andExpect(status().isOk());
    }

    @Test
    void activateCard_ValidRequest_ReturnsOk() throws Exception {
        UUID cardId = UUID.randomUUID();

        mockMvc.perform(post("/api/cards/adminactivate?cardId={cardId}", cardId))
                .andExpect(status().isOk());

        Mockito.verify(cardService).activateCard(cardId);
    }

    @Test
    void transferMoney_ValidRequest_ReturnsOk() throws Exception {
        TransferDto transfer = new TransferDto();

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk());

        Mockito.verify(cardService).transferMoney(any(TransferDto.class));
    }

    @Test
    void getCardsWithFilter_ValidRequest_ReturnsOk() throws Exception {
        CardFilterRequest filter = new CardFilterRequest();

        mockMvc.perform(post("/api/cards/sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk());

        Mockito.verify(cardService).findAllFilteredCards(any(CardFilterRequest.class));
    }

    @Test
    void getCardById_Exists_ReturnsOk() throws Exception {
        UUID cardId = UUID.randomUUID();
        Card card = new Card();

        Mockito.when(cardService.getById(cardId)).thenReturn(card);

        mockMvc.perform(get("/api/cards/{cardId}", cardId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCard_ValidId_ReturnsOk() throws Exception {
        UUID cardId = UUID.randomUUID();

        mockMvc.perform(delete("/api/cards/{cardId}", cardId))
                .andExpect(status().isOk());

        Mockito.verify(cardService).deleteById(cardId);
    }
}
