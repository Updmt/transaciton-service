package com.testpayments.transacitonservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;
    private String currency;
    private BigDecimal balance;
    private UUID merchantId;
}
