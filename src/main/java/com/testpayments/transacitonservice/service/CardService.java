package com.testpayments.transacitonservice.service;

import com.testpayments.transacitonservice.dto.TopUpRequest;
import com.testpayments.transacitonservice.entity.Card;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CardService {

    Mono<Card> findCardByCardNumberAndCurrency(String cardNumber, String currency);
    Mono<Card> findCardById(UUID id);
    Mono<Card> createCard(Card card);
    Mono<Card> updateCard(Card card);
    Mono<Card> updateCardBalance(Card card, TopUpRequest topUpRequest);
}
