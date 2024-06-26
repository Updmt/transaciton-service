package com.testpayments.transacitonservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "webhooks")
public class Webhook {

    @Id
    private UUID id;
    private WebhookResponseStatus responseStatus;
    private Status status;
    private String responseBody;
    private String requestBody;
    private String notificationUrl;
    private Integer attemptAmount;
    private UUID transactionId;
}
