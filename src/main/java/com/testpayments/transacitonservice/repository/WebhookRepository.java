package com.testpayments.transacitonservice.repository;

import com.testpayments.transacitonservice.entity.Status;
import com.testpayments.transacitonservice.entity.Webhook;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface WebhookRepository extends R2dbcRepository<Webhook, UUID> {

    @Modifying
    @Query("INSERT INTO webhooks (status, request_body, notification_url, attempt_amount, transaction_id) " +
            "VALUES (:status, CAST(:requestBody AS JSONB), :notificationUrl, :attemptAmount, :transactionId)")
    Mono<Webhook> saveWebhook(Status status, String requestBody, String notificationUrl, Integer attemptAmount, UUID transactionId);

    @Query("SELECT MAX(attempt_amount) FROM webhooks WHERE transaction_id = :transactionId")
    Mono<Integer> findMaxAttemptCountByTransactionId(UUID transactionId);
}
