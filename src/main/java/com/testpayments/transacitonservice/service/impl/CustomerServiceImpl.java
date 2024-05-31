package com.testpayments.transacitonservice.service.impl;

import com.testpayments.transacitonservice.entity.Customer;
import com.testpayments.transacitonservice.repository.CustomerRepository;
import com.testpayments.transacitonservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Mono<Customer> findCustomerByFirstNameAndLastNameAndCountry(String firstName, String lastName, String county) {
        return customerRepository.findCustomerByFirstNameAndLastNameAndCountry(firstName, lastName, county);
    }

    @Override
    public Mono<Customer> findCustomerById(UUID id) {
        return customerRepository.findCustomerById(id);
    }

    @Override
    public Mono<Customer> createCustomer(Customer customer) {
        return customerRepository.save(customer)
                .doOnSuccess(u -> log.info("Customer was created with id {}", customer.getId()))
                .doOnError(error -> log.error("Error when trying to save customer", error));
    }
}
