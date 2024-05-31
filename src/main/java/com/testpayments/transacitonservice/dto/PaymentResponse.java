package com.testpayments.transacitonservice.dto;

import com.testpayments.transacitonservice.entity.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class PaymentResponse {

    private UUID transactionId;
    private Status status;
    private String message;
}
