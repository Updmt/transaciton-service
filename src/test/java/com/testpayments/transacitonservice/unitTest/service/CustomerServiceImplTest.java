package com.testpayments.transacitonservice.unitTest.service;

import com.testpayments.transacitonservice.entity.Customer;
import com.testpayments.transacitonservice.repository.CustomerRepository;
import com.testpayments.transacitonservice.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Test
    void findCustomerByFirstNameAndLastNameAndCountry_ok() {
        String firstName = "John";
        String lastName = "Doe";
        String country = "USA";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setCountry(country);

        when(customerRepository.findCustomerByFirstNameAndLastNameAndCountry(firstName, lastName, country)).thenReturn(Mono.just(customer));

        Mono<Customer> customerMono = customerService.findCustomerByFirstNameAndLastNameAndCountry(firstName, lastName, country);

        StepVerifier.create(customerMono)
                .expectNextMatches(foundCustomer -> foundCustomer.getFirstName().equals(firstName) &&
                        foundCustomer.getLastName().equals(lastName) &&
                        foundCustomer.getCountry().equals(country))
                .verifyComplete();
    }

    @Test
    void findCustomerByFirstNameAndLastNameAndCountry_throwException() {
        String firstName = "John";
        String lastName = "Doe";
        String country = "USA";
        RuntimeException expectedException = new RuntimeException("Database error");

        when(customerRepository.findCustomerByFirstNameAndLastNameAndCountry(firstName, lastName, country)).thenReturn(Mono.error(expectedException));

        Mono<Customer> customerMono = customerService.findCustomerByFirstNameAndLastNameAndCountry(firstName, lastName, country);

        StepVerifier.create(customerMono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findCustomerById_ok() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setCountry("USA");

        when(customerRepository.findCustomerById(customerId)).thenReturn(Mono.just(customer));

        Mono<Customer> customerMono = customerService.findCustomerById(customerId);

        StepVerifier.create(customerMono)
                .expectNextMatches(foundCustomer -> foundCustomer.getId().equals(customerId) &&
                        foundCustomer.getFirstName().equals("John") &&
                        foundCustomer.getLastName().equals("Doe") &&
                        foundCustomer.getCountry().equals("USA"))
                .verifyComplete();
    }

    @Test
    void findCustomerById_throwException() {
        UUID customerId = UUID.randomUUID();
        RuntimeException exception = new RuntimeException("Database error");

        when(customerRepository.findCustomerById(customerId)).thenReturn(Mono.error(exception));

        Mono<Customer> customerMono = customerService.findCustomerById(customerId);

        StepVerifier.create(customerMono)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database error"))
                .verify();
    }

    @Test
    void createCustomer_Success() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setCountry("USA");

        when(customerRepository.save(customer)).thenReturn(Mono.just(customer));

        Mono<Customer> customerMono = customerService.createCustomer(customer);

        StepVerifier.create(customerMono)
                .expectNextMatches(createdCustomer -> createdCustomer.getId().equals(customerId) &&
                        createdCustomer.getFirstName().equals("John") &&
                        createdCustomer.getLastName().equals("Doe") &&
                        createdCustomer.getCountry().equals("USA"))
                .verifyComplete();
    }

    @Test
    void createCustomer_ThrowsException() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        RuntimeException exception = new RuntimeException("Database error");

        when(customerRepository.save(customer)).thenReturn(Mono.error(exception));

        Mono<Customer> customerMono = customerService.createCustomer(customer);

        StepVerifier.create(customerMono)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database error"))
                .verify();
    }
}
