package com.testpayments.transacitonservice.service;

import com.testpayments.transacitonservice.dto.WithdrawalRequest;
import com.testpayments.transacitonservice.entity.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountService {

    Mono<Account> findAccountByMerchantIdAndCurrency(UUID merchantId, String currency);
    Mono<Account> findById(UUID id);
    Mono<Account> findAccountByIdForUpdate(UUID id);
    Mono<Account> updateAccount(Account account);
    Flux<Account> findAccountsByMerchantId(UUID merchantId);
    Mono<Account> updateAccountBalance(Account account, WithdrawalRequest withdrawalRequest);
}
