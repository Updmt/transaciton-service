package com.testpayments.transacitonservice.service.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.testpayments.transacitonservice.dto.TopUpCardDataDto;
import com.testpayments.transacitonservice.dto.CustomerDataDto;
import com.testpayments.transacitonservice.dto.WebhookDto;
import com.testpayments.transacitonservice.entity.Account;
import com.testpayments.transacitonservice.entity.Card;
import com.testpayments.transacitonservice.entity.Customer;
import com.testpayments.transacitonservice.entity.Status;
import com.testpayments.transacitonservice.entity.Transaction;
import com.testpayments.transacitonservice.entity.Type;
import com.testpayments.transacitonservice.entity.Webhook;
import com.testpayments.transacitonservice.entity.WebhookResponseStatus;
import com.testpayments.transacitonservice.exception.CustomExhaustedException;
import com.testpayments.transacitonservice.service.AccountService;
import com.testpayments.transacitonservice.service.CardService;
import com.testpayments.transacitonservice.service.CustomerService;
import com.testpayments.transacitonservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookJob {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CardService cardService;
    private final CustomerService customerService;
    private final WebhookService webhookService;
    private final TransactionalOperator transactionalOperator;


    @Scheduled(fixedRate = 5000, initialDelay = 10000)
    public Mono<Void> assignStatusToTransaction() {
        return transactionService.findAllTransactionsByStatus(Status.IN_PROCESS)
                .doOnNext(transaction -> log.info("Processing transaction with ID: " + transaction.getId() + " and status: " + transaction.getStatus()))
                .flatMap(transaction ->
                        transactionalOperator.transactional(
                                        Mono.just(transaction)
                                                .map(transactionService::assignRandomStatus)
                                                .flatMap(transactionService::updateTransaction)
                                                .flatMap(updatedTransaction ->
                                                        accountService.findAccountByIdForUpdate(transaction.getAccountId())
                                                                .flatMap(account -> updateAccountBalance(transaction, account)))
                                )
                                .doOnSuccess(aVoid -> log.info("Transaction successfully completed for transaction id {}", transaction.getId()))
                                .flatMap(account -> sendWebhook(transaction))
                )
                .then();
    }

    private Mono<?> updateAccountBalance(Transaction transaction, Account account) {
        return cardService.findCardById(transaction.getCardId())
                .flatMap(card -> {
                    BigDecimal currentAccountBalance = account.getBalance();
                    BigDecimal currentCardBalance = card.getBalance();
                    BigDecimal transactionAmount = transaction.getAmount();

                    if (transaction.getStatus().equals(Status.APPROVED)) {
                        log.info("Transaction with id {} was approved", transaction.getId());
                        if (transaction.getType().equals(Type.TOP_UP)) {
                            account.setBalance(currentAccountBalance.add(transactionAmount));
                            log.info("Account balance was increased for transaction with id {}", transaction.getId());
                            return accountService.updateAccount(account);
                        } else {
                            card.setBalance(currentCardBalance.add(transactionAmount));
                            log.info("Card balance was increased for transaction with id {}", transaction.getId());
                            return cardService.updateCard(card);
                        }

                    }

                    if (transaction.getStatus().equals(Status.FAILED)) {
                        log.info("Transaction with id {} was failed", transaction.getId());
                        if (transaction.getType().equals(Type.TOP_UP)) {
                            card.setBalance(currentCardBalance.add(transactionAmount));
                            log.info("Card balance was increased for transaction with id {}", transaction.getId());
                            return cardService.updateCard(card);
                        } else {
                            account.setBalance(currentAccountBalance.add(transactionAmount));
                            log.info("Account balance was increased for transaction with id {}", transaction.getId());
                            return accountService.updateAccount(account);
                        }
                    }

                    return Mono.error(new IllegalStateException("Invalid transaction status"));
                });
    }

    private Mono<String> sendWebhook(Transaction transaction) {
        return cardService.findCardById(transaction.getCardId())
                .flatMap(card -> customerService.findCustomerById(card.getCustomerId())
                        .flatMap(customer -> sendWebhookWithRetries(transaction, card, customer))
                );
    }

    private Mono<String> sendWebhookWithRetries(Transaction transaction, Card card, Customer customer) {
        UUID transactionId = transaction.getId();
        return webhookService.findMaxAttemptCountByTransactionId(transactionId)
                .defaultIfEmpty(0)
                .flatMap(maxAttempt -> Mono.defer(() -> {
                    int attemptAmount = maxAttempt + 1;
                    WebhookDto webhookDto = createWebhookRequest(transaction, card, customer);
                    Webhook webhook = createWebhook(webhookDto, transaction);
                    webhook.setAttemptAmount(attemptAmount);

                    return webhookService.sendWebhook(webhook)
                            .doOnSuccess(response -> {
                                log.info("Webhook sent successfully for transaction with id {}", transactionId);
                                saveWebhookWhenSuccess(webhook, response);
                            })
                            .onErrorResume(error -> handleError(webhook, error, transactionId));
                }))
                .retryWhen(Retry.backoff(4, Duration.ofSeconds(1))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new CustomExhaustedException()))
                .onErrorResume(CustomExhaustedException.class, ex -> Mono.empty());
    }

    private Mono<String> handleError(Webhook webhook, Throwable error, UUID transactionId) {
        if (error instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) error;
            saveWebhookWhenFailed(webhook, ex.getResponseBodyAsString());
            log.info("Webhook sending failed for webhook with transaction id {}", transactionId);
            return Mono.error(new RuntimeException("Server error, retrying...", ex));
        } else {
            log.error("Error sending webhook: {}", error.getMessage(), error);
            saveWebhookWhenFailed(webhook, null);
            return Mono.error(new RuntimeException("Client error, retrying...", error));
        }
    }

    private void saveWebhookWhenSuccess(Webhook webhook, String responseBody) {
        webhook.setResponseStatus(WebhookResponseStatus.SUCCESSFUL);
        webhook.setResponseBody(responseBody);
        webhookService.save(webhook).subscribe();
    }

    private void saveWebhookWhenFailed(Webhook webhook, String responseBody) {
        webhook.setResponseStatus(WebhookResponseStatus.FAILED);
        webhook.setResponseBody(responseBody);
        webhookService.save(webhook).subscribe();
    }

    private WebhookDto createWebhookRequest(Transaction transaction, Card card, Customer customer) {
        return WebhookDto.builder()
                .paymentMethod("Card")
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type("Transaction")
                .transactionId(transaction.getId())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .topUpCardDataDto(TopUpCardDataDto.builder()
                        .cardNumber(card.getCardNumber())
                        .build())
                .language(transaction.getLanguage())
                .customerDataDto(CustomerDataDto.builder()
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .build())
                .status(transaction.getStatus())
                .message("OK")
                .build();
    }

    private Webhook createWebhook(WebhookDto webhookDto, Transaction transaction) {
        String jsonRequest = convertDtoToJson(webhookDto);
        return Webhook.builder()
                .status(webhookDto.getStatus())
                .requestBody(jsonRequest)
                .notificationUrl(transaction.getNotificationUrl())
                .transactionId(transaction.getId())
                .build();
    }

    private String convertDtoToJson(WebhookDto webhookDto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(webhookDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting WebhookDto to JSON", e);
        }
    }
}
