package com.testpayments.transacitonservice.unitTest.service;

import com.testpayments.transacitonservice.dto.TopUpRequest;
import com.testpayments.transacitonservice.entity.Card;
import com.testpayments.transacitonservice.exception.CustomNotFoundException;
import com.testpayments.transacitonservice.exception.InsufficientFundsException;
import com.testpayments.transacitonservice.repository.CardRepository;
import com.testpayments.transacitonservice.service.impl.CardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTest {

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private CardRepository cardRepository;

    @Test
    void findCardByCardNumberAndCurrency_ok() {
        String cardNumber = "1234567890123456";
        String currency = "USD";

        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setCardNumber(cardNumber);
        card.setCurrency(currency);

        when(cardRepository.findCardByCardNumberAndCurrency(cardNumber, currency)).thenReturn(Mono.just(card));

        Mono<Card> cardMono = cardService.findCardByCardNumberAndCurrency(cardNumber, currency);

        StepVerifier.create(cardMono)
                .expectNextMatches(foundCard -> foundCard.getCardNumber().equals(cardNumber) &&
                        foundCard.getCurrency().equals(currency))
                .verifyComplete();
    }

    @Test
    void findCardByCardNumberAndCurrency_throwException() {
        String cardNumber = "1234567890123456";
        String currency = "USD";

        RuntimeException expectedException = new RuntimeException("Database error");

        when(cardRepository.findCardByCardNumberAndCurrency(cardNumber, currency)).thenReturn(Mono.error(expectedException));

        Mono<Card> cardMono = cardService.findCardByCardNumberAndCurrency(cardNumber, currency);

        StepVerifier.create(cardMono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findCardById_ok() {
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setId(cardId);

        when(cardRepository.findCardById(cardId)).thenReturn(Mono.just(card));

        Mono<Card> cardMono = cardService.findCardById(cardId);

        StepVerifier.create(cardMono)
                .expectNextMatches(foundCard -> foundCard.getId().equals(cardId))
                .verifyComplete();
    }

    @Test
    void findCardById_NotFound() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findCardById(cardId)).thenReturn(Mono.empty());

        Mono<Card> cardMono = cardService.findCardById(cardId);

        StepVerifier.create(cardMono)
                .expectErrorMatches(exception -> exception instanceof CustomNotFoundException &&
                        exception.getMessage().equals("Card not found with ID: " + cardId))
                .verify();
    }

    @Test
    void createCard_ok() {
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setId(cardId);

        when(cardRepository.save(card)).thenReturn(Mono.just(card));

        Mono<Card> cardMono = cardService.createCard(card);

        StepVerifier.create(cardMono)
                .expectNextMatches(createdCard -> createdCard.getId().equals(cardId))
                .verifyComplete();
    }

    @Test
    void createCard_throwException() {
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setId(cardId);
        RuntimeException runtimeException = new RuntimeException("Database error");

        when(cardRepository.save(card)).thenReturn(Mono.error(runtimeException));

        Mono<Card> cardMono = cardService.createCard(card);

        StepVerifier.create(cardMono)
                .expectErrorMatches(exception -> exception instanceof RuntimeException &&
                        exception.getMessage().equals("Database error"))
                .verify();
    }

    @Test
    void updateCard_Success() {
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setId(cardId);
        card.setBalance(new BigDecimal("100.00"));

        when(cardRepository.save(card)).thenReturn(Mono.just(card));

        Mono<Card> cardMono = cardService.updateCard(card);

        StepVerifier.create(cardMono)
                .expectNextMatches(updatedCard -> updatedCard.getId().equals(cardId) &&
                        updatedCard.getBalance().compareTo(new BigDecimal("100.00")) == 0)
                .verifyComplete();
    }

    @Test
    void updateCard_ThrowsRuntimeException() {
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setId(cardId);
        RuntimeException runtimeException = new RuntimeException("Database error");

        when(cardRepository.save(card)).thenReturn(Mono.error(runtimeException));

        Mono<Card> cardMono = cardService.updateCard(card);

        StepVerifier.create(cardMono)
                .expectErrorMatches(exception -> exception instanceof RuntimeException &&
                        exception.getMessage().equals("Database error"))
                .verify();
    }

    @Test
    void updateCardBalance_ok() {
        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setBalance(new BigDecimal("100.00"));

        TopUpRequest topUpRequest = new TopUpRequest();
        topUpRequest.setAmount(25);

        when(cardRepository.save(card)).thenReturn(Mono.just(card));

        Mono<Card> result = cardService.updateCardBalance(card, topUpRequest);

        StepVerifier.create(result)
                .expectNextMatches(updatedCard ->
                        updatedCard.getBalance().compareTo(new BigDecimal("75.00")) == 0)
                .verifyComplete();
    }

    @Test
    void updateCardBalance_throwException() {
        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setBalance(new BigDecimal("10.00"));

        TopUpRequest topUpRequest = new TopUpRequest();
        topUpRequest.setAmount(25);

        Mono<Card> result = cardService.updateCardBalance(card, topUpRequest);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InsufficientFundsException &&
                        throwable.getMessage().equals("Not enough money on balance"))
                .verify();
    }
}
