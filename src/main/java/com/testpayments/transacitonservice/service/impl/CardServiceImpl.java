package com.testpayments.transacitonservice.service.impl;

import com.testpayments.transacitonservice.dto.TopUpRequest;
import com.testpayments.transacitonservice.entity.Card;
import com.testpayments.transacitonservice.exception.CustomNotFoundException;
import com.testpayments.transacitonservice.exception.InsufficientFundsException;
import com.testpayments.transacitonservice.repository.CardRepository;
import com.testpayments.transacitonservice.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private static final Integer ZERO_BALANCE = 0;

    private final CardRepository cardRepository;

    @Override
    public Mono<Card> findCardByCardNumberAndCurrency(String cardNumber, String currency) {
        return cardRepository.findCardByCardNumberAndCurrency(cardNumber, currency)
                .doOnSuccess(u -> log.info("Card with number {} and currency {} was found", cardNumber, currency));
    }

    @Override
    public Mono<Card> findCardById(UUID id) {
        return cardRepository.findCardById(id)
                .doOnSuccess(u -> log.info("Card was found"))
                .doOnError(error -> log.error("Error when trying to find card", error))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("No card found with ID: {}", id);
                    return Mono.error(new CustomNotFoundException("Card not found with ID: " + id));
                }));
    }

    @Override
    public Mono<Card> createCard(Card card) {
        return cardRepository.save(card)
                .doOnSuccess(u -> log.info("Card was created with id {}", card.getId()))
                .doOnError(error -> log.error("Error when trying to save card", error));
    }

    @Override
    public Mono<Card> updateCard(Card card) {
        return cardRepository.save(card);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Mono<Card> updateCardBalance(Card card, TopUpRequest topUpRequest) {
        BigDecimal currentBalance = card.getBalance();
        BigDecimal topUpAmount = new BigDecimal(topUpRequest.getAmount());
        if (currentBalance.compareTo(topUpAmount) >= ZERO_BALANCE) {
            card.setBalance(currentBalance.subtract(topUpAmount));
            return updateCard(card)
                    .doOnSuccess(updatedCard -> log.info("Card balance was reduced by {}", topUpAmount));
        } else {
            return Mono.error(new InsufficientFundsException("Not enough money on balance"));
        }
    }
}
