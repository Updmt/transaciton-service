package com.testpayments.transacitonservice.service;

import com.testpayments.transacitonservice.entity.Customer;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerService {

    Mono<Customer> findCustomerByFirstNameAndLastNameAndCountry(String firstName, String lastName, String county);
    Mono<Customer> findCustomerById(UUID id);
    Mono<Customer> createCustomer(Customer customer);
}
