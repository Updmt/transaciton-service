package com.testpayments.transacitonservice.repository;

import com.testpayments.transacitonservice.entity.Customer;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerRepository extends R2dbcRepository<Customer, UUID> {

    Mono<Customer> findCustomerByFirstNameAndLastNameAndCountry(String firstName, String lastName, String county);
    Mono<Customer> findCustomerById(UUID id);
}
