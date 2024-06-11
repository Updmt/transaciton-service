package com.testpayments.transacitonservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractPaymentRequest {

    @JsonProperty("payment_method")
    private String paymentMethod;

    private Integer amount;

    private String currency;

    private String language;

    @JsonProperty("notification_url")
    private String notificationUrl;

    @JsonProperty("customer")
    private CustomerDataDto customerDataDto;
}
