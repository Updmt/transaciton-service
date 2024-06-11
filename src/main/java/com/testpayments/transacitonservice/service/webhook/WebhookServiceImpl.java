package com.testpayments.transacitonservice.service.webhook;

import com.testpayments.transacitonservice.entity.Webhook;
import com.testpayments.transacitonservice.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final WebhookRepository webhookRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<Webhook> save(Webhook webhook) {
        return webhookRepository.save(webhook);
    }

    //todo для этого метода нет юнит тестов
    @Override
    public Mono<String> sendWebhook(Webhook webhook) {
        return webClientBuilder.build()
                .post()
                .uri(webhook.getNotificationUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(webhook.getRequestBody())
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    public Mono<Integer> findMaxAttemptCountByTransactionId(UUID transactionId) {
        return webhookRepository.findMaxAttemptCountByTransactionId(transactionId);
    }
}
