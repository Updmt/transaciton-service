package com.testpayments.transacitonservice.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Table(name = "cards")
public class Card {

    @Id
    private UUID id;
    private String cardNumber;
    private LocalDateTime expDate;
    private String cvv;
    private String currency;
    private BigDecimal balance;
    private UUID customerId;
}
