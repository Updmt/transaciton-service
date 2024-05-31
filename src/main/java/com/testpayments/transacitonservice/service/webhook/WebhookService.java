package com.testpayments.transacitonservice.service.webhook;

import com.testpayments.transacitonservice.entity.Status;
import com.testpayments.transacitonservice.entity.Webhook;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WebhookService {

    Mono<Webhook> save(Webhook webhook);
    Mono<String> sendWebhook(Webhook webhook);
    Mono<Integer> findMaxAttemptCountByTransactionId(UUID transactionId);
}
