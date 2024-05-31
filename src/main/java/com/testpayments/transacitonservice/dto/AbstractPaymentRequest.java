package com.testpayments.transacitonservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
