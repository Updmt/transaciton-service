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
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest extends AbstractPaymentRequest {

    @JsonProperty("card_data")
    private WithdrawalCardDataDto withdrawalCardDataDto;
}
