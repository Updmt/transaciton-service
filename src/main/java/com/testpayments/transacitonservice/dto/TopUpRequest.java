package com.testpayments.transacitonservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopUpRequest extends AbstractPaymentRequest {

    @JsonProperty("card_data")
    private TopUpCardDataDto topUpCardDataDto;
}
