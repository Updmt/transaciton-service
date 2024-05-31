package com.testpayments.transacitonservice.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Builder
@Table(name = "customers")
public class Customer {

    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String country;
}
