package com.testpayments.transacitonservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.testpayments.transacitonservice.entity.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class WebhookDto {

    @JsonProperty("payment_method")
    private String paymentMethod;

    private BigDecimal amount;

    private String currency;

    private String type;

    private UUID transactionId;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;

    @JsonProperty("card_data")
    private TopUpCardDataDto topUpCardDataDto;

    private String language;

    @JsonProperty("customer")
    private CustomerDataDto customerDataDto;

    private Status status;

    private String message;
}
