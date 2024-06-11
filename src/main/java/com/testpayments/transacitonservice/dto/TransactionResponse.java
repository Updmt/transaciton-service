package com.testpayments.transacitonservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    @JsonProperty("payment_method")
    private String paymentMethod;

    private BigDecimal amount;

    private String currency;

    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("notification_url")
    private String notificationUrl;

    @JsonProperty("card_data")
    private TopUpCardDataDto topUpCardDataDto;

    private String language;

    @JsonProperty("customer")
    private CustomerDataDto customerDataDto;

    private String message;
}
