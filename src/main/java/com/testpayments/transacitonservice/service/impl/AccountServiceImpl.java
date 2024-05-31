package com.testpayments.transacitonservice.service.impl;

import com.testpayments.transacitonservice.dto.WithdrawalRequest;
import com.testpayments.transacitonservice.entity.Account;
import com.testpayments.transacitonservice.exception.CustomNotFoundException;
import com.testpayments.transacitonservice.exception.InsufficientFundsException;
import com.testpayments.transacitonservice.repository.AccountRepository;
import com.testpayments.transacitonservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final Integer ZERO_BALANCE = 0;

    private final AccountRepository accountRepository;

    @Override
    public Mono<Account> findAccountByMerchantIdAndCurrency(UUID merchantId, String currency) {
        return accountRepository.findAccountByMerchantIdAndCurrency(merchantId, currency)
                .doOnSuccess(account -> log.info("Merchants account with merchant id {} and currency {} was found", merchantId, currency));
    }

    @Override
    public Mono<Account> findById(UUID id) {
        return accountRepository.findById(id);
    }

    @Override
    public Mono<Account> findAccountByIdForUpdate(UUID id) {
        return accountRepository.findAccountByIdForUpdate(id)
                .doOnSuccess(account -> log.info("Row was locked"));
    }

    @Override
    public Mono<Account> updateAccount(Account account) {
        return accountRepository.save(account)
                .doOnSuccess(updatedAccount -> log.info("Account updated for ID: {}", updatedAccount.getId()))
                .doOnError(error -> log.error("Error updating account", error));
    }

    @Override
    public Flux<Account> findAccountsByMerchantId(UUID merchantId) {
        return accountRepository.findAccountsByMerchantId(merchantId)
                .doOnNext(account -> log.info("Accounts were found"))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Accounts were nor found")));
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Mono<Account> updateAccountBalance(Account account, WithdrawalRequest withdrawalRequest) {
        BigDecimal currentBalance = account.getBalance();
        BigDecimal payOutAmount = new BigDecimal(withdrawalRequest.getAmount());
        if (currentBalance.compareTo(payOutAmount) >= ZERO_BALANCE) {
            account.setBalance(currentBalance.subtract(payOutAmount));
            return updateAccount(account)
                    .doOnSuccess(updatedCard -> log.info("Account balance was reduced by {}", payOutAmount));
        }
        else {
            return Mono.error(new InsufficientFundsException("Not enough money on balance"));
        }
    }
}
