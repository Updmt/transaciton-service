package com.testpayments.transacitonservice.unitTest.service;

import com.testpayments.transacitonservice.entity.Status;
import com.testpayments.transacitonservice.entity.Webhook;
import com.testpayments.transacitonservice.repository.WebhookRepository;
import com.testpayments.transacitonservice.service.webhook.WebhookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebhookServiceImplTest {

    @InjectMocks
    private WebhookServiceImpl webhookService;
    @Mock
    private WebhookRepository webhookRepository;

    @Test
    void saveWebhook_ok() {
        Webhook webhook = Webhook.builder()
                .id(UUID.randomUUID())
                .status(Status.APPROVED)
                .responseBody("Response from server")
                .build();

        when(webhookRepository.save(webhook)).thenReturn(Mono.just(webhook));

        Mono<Webhook> result = webhookService.save(webhook);

        StepVerifier.create(result)
                .expectNextMatches(savedWebhook -> savedWebhook.getId().equals(webhook.getId()) &&
                        savedWebhook.getStatus() == Status.APPROVED &&
                        savedWebhook.getResponseBody().equals("Response from server"))
                .verifyComplete();
    }

    @Test
    void saveWebhook_throwException() {
        Webhook webhook = new Webhook();

        when(webhookRepository.save(webhook)).thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<Webhook> result = webhookService.save(webhook);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database error"))
                .verify();
    }

    @Test
    void findMaxAttemptCountByTransactionId_Successful() {
        UUID transactionId = UUID.randomUUID();
        int maxAttemptCount = 1;

        when(webhookRepository.findMaxAttemptCountByTransactionId(transactionId)).thenReturn(Mono.just(maxAttemptCount));

        Mono<Integer> result = webhookService.findMaxAttemptCountByTransactionId(transactionId);

        StepVerifier.create(result)
                .expectNext(maxAttemptCount)
                .verifyComplete();
    }

    @Test
    void findMaxAttemptCountByTransactionId_throwException() {
        UUID transactionId = UUID.randomUUID();

        when(webhookRepository.findMaxAttemptCountByTransactionId(transactionId)).thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<Integer> result = webhookService.findMaxAttemptCountByTransactionId(transactionId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database error"))
                .verify();
    }
}
