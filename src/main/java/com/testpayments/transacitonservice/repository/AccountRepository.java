package com.testpayments.transacitonservice.repository;

import com.testpayments.transacitonservice.entity.Account;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountRepository extends R2dbcRepository<Account, UUID> {

    Mono<Account> findAccountByMerchantIdAndCurrency(UUID merchantId, String currency);
    @Query("SELECT * FROM accounts where id = :id FOR UPDATE ")
    Mono<Account> findAccountByIdForUpdate(UUID id);
    Flux<Account> findAccountsByMerchantId(UUID merchantId);
}
