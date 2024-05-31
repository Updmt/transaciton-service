package com.testpayments.transacitonservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Table(name = "merchants")
public class Merchant {

    @Id
    private UUID id;
    private String secretKey;
    private LocalDateTime createdAt;
    private String companyRecognition;
    private String country;
}
