package com.testpayments.transacitonservice.repository;

import com.testpayments.transacitonservice.entity.Card;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CardRepository extends R2dbcRepository<Card, UUID> {

    Mono<Card> findCardByCardNumberAndCurrency(String cardNumber, String currency);
    @Query("SELECT * FROM cards WHERE card_number = :cardNumber AND currency = :currency FOR UPDATE")
    Mono<Card> findCardByCardNumberAndCurrencyForUpdate(String cardNumber, String currency);
    Mono<Card> findCardById(UUID id);
}
