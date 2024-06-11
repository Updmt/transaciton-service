package com.testpayments.transacitonservice.unitTest.service;

import com.testpayments.transacitonservice.dto.WithdrawalRequest;
import com.testpayments.transacitonservice.entity.Account;
import com.testpayments.transacitonservice.exception.CustomNotFoundException;
import com.testpayments.transacitonservice.exception.InsufficientFundsException;
import com.testpayments.transacitonservice.repository.AccountRepository;
import com.testpayments.transacitonservice.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;

    @Test
    void findAccountByMerchantIdAndCurrency_ok() {
        UUID merchantId = UUID.fromString("a3bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        UUID accountId = UUID.fromString("a4bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        String currency = "USD";

        Account account = new Account();
        account.setId(accountId);
        account.setCurrency(currency);
        account.setMerchantId(merchantId);

        when(accountRepository.findAccountByMerchantIdAndCurrency(merchantId, currency)).thenReturn(Mono.just(account));

        Mono<Account> accountMono = accountService.findAccountByMerchantIdAndCurrency(merchantId, currency);

        StepVerifier.create(accountMono)
                .expectNextMatches(result -> result.getId().equals(accountId) &&
                        result.getCurrency().equals(currency) &&
                        result.getMerchantId().equals(merchantId))
                .verifyComplete();
    }

    @Test
    void findAccountByMerchantIdAndCurrency_throwException() {
        UUID merchantId = UUID.fromString("a3bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        String currency = "USD";
        RuntimeException expectedException = new RuntimeException("Database error");

        when(accountRepository.findAccountByMerchantIdAndCurrency(merchantId, currency)).thenReturn(Mono.error(expectedException));

        Mono<Account> accountMono = accountService.findAccountByMerchantIdAndCurrency(merchantId, currency);

        StepVerifier.create(accountMono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findById_ok() {
        UUID accountId = UUID.fromString("a4bb189e-8bf9-4c8d-9d5d-139aad8e6633");

        Account account = new Account();
        account.setId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(Mono.just(account));

        Mono<Account> accountMono = accountService.findById(accountId);
        StepVerifier.create(accountMono)
                .expectNextMatches(foundAccount -> foundAccount.getId().equals(accountId))
                .verifyComplete();
    }

    @Test
    void findById_throwException() {
        UUID accountId = UUID.fromString("a4bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        RuntimeException expectedException = new RuntimeException("Database error");

        when(accountRepository.findById(accountId)).thenReturn(Mono.error(expectedException));

        Mono<Account> accountMono = accountService.findById(accountId);
        StepVerifier.create(accountMono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findAccountByIdForUpdate_ok() {
        UUID accountId = UUID.fromString("a4bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        Account account = new Account();
        account.setId(accountId);

        when(accountRepository.findAccountByIdForUpdate(accountId)).thenReturn(Mono.just(account));

        Mono<Account> result = accountService.findAccountByIdForUpdate(accountId);

        StepVerifier.create(result)
                .expectNextMatches(foundAccount -> foundAccount.getId().equals(accountId))
                .expectComplete()
                .verify();
    }

    @Test
    void findAccountByIdForUpdate_throwException() {
        UUID accountId = UUID.fromString("a4bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        RuntimeException expectedException = new RuntimeException("Database error");

        when(accountRepository.findAccountByIdForUpdate(accountId)).thenReturn(Mono.error(expectedException));

        Mono<Account> accountMono = accountService.findAccountByIdForUpdate(accountId);

        StepVerifier.create(accountMono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void updateAccount_ok() {
        UUID merchantId = UUID.fromString("a3bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        UUID accountId = UUID.fromString("a4bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        String currency = "USD";

        Account account = new Account();
        account.setId(accountId);
        account.setCurrency(currency);
        account.setMerchantId(merchantId);

        when(accountRepository.save(account)).thenReturn(Mono.just(account));

        Mono<Account> accountMono = accountService.updateAccount(account);

        StepVerifier
                .create(accountMono)
                .expectNextMatches(savedAccount ->
                                savedAccount.getId().equals(account.getId()) &&
                                savedAccount.getCurrency().equals(account.getCurrency()) &&
                                savedAccount.getMerchantId().equals(account.getMerchantId()))
                .verifyComplete();
    }

    @Test
    void updateAccount_throwException() {
        UUID merchantId = UUID.fromString("a3bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        UUID accountId = UUID.fromString("a4bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        String currency = "USD";

        Account account = new Account();
        account.setId(accountId);
        account.setCurrency(currency);
        account.setMerchantId(merchantId);

        RuntimeException expectedException = new RuntimeException("Database error");

        when(accountRepository.save(account)).thenReturn(Mono.error(expectedException));

        Mono<Account> accountMono = accountService.updateAccount(account);

        StepVerifier.create(accountMono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findAccountsByMerchantId_ok() {
        UUID merchantId = UUID.fromString("a3bb189e-8bf9-4c8d-9d5d-139aad8e6633");
        Account account1 = new Account();
        Account account2 = new Account();

        when(accountRepository.findAccountsByMerchantId(merchantId)).thenReturn(Flux.just(account1, account2));

        Flux<Account> result = accountService.findAccountsByMerchantId(merchantId);

        StepVerifier.create(result)
                .expectNext(account1)
                .expectNext(account2)
                .verifyComplete();
    }

    @Test
    void findAccountsByMerchantId_throwException() {
        UUID merchantId = UUID.fromString("a3bb189e-8bf9-4c8d-9d5d-139aad8e6633");

        when(accountRepository.findAccountsByMerchantId(merchantId)).thenReturn(Flux.empty());

        Flux<Account> result = accountService.findAccountsByMerchantId(merchantId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundException &&
                        throwable.getMessage().equals("Accounts were nor found"))
                .verify();
    }

    @Test
    void updateAccountBalance_ok() {
        Account account = new Account();
        account.setBalance(new BigDecimal("100.00"));

        WithdrawalRequest withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setAmount(25);

        when(accountRepository.save(account)).thenReturn(Mono.just(account));

        Mono<Account> accountMono = accountService.updateAccountBalance(account, withdrawalRequest);

        StepVerifier
                .create(accountMono)
                .expectNextMatches(updatedAccount ->
                        updatedAccount.getBalance().compareTo(new BigDecimal("75.00")) == 0)
                .verifyComplete();
    }

    @Test
    void updateAccountBalance_throwException() {
        Account account = new Account();
        account.setBalance(new BigDecimal("10.00"));

        WithdrawalRequest withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setAmount(25);

        Mono<Account> accountMono = accountService.updateAccountBalance(account, withdrawalRequest);

        StepVerifier.create(accountMono)
                .expectErrorMatches(throwable -> throwable instanceof InsufficientFundsException &&
                        throwable.getMessage().equals("Not enough money on balance"))
                .verify();
    }
}
