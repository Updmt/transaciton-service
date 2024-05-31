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
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String currency;
    private BigDecimal amount;
    private String notificationUrl;
    private String language;
    private Status status;
    private Type type;
    private UUID cardId;
    private UUID accountId;
}
