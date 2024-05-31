package com.testpayments.transacitonservice.repository;

import com.testpayments.transacitonservice.entity.Status;
import com.testpayments.transacitonservice.entity.Transaction;
import com.testpayments.transacitonservice.entity.Type;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionRepository extends R2dbcRepository<Transaction, UUID> {

    @Query("select * from transactions where created_at >= :startDate and created_at <= :endDate " +
            "and account_id = :accountId and type = :transactionType limit :size offset :offset")
    Flux<Transaction> findByDateRangeAndType(LocalDateTime startDate, LocalDateTime endDate, UUID accountId, Type transactionType, int size, long offset);
    Mono<Transaction> findByIdAndType(UUID transactionId, Type transactionType);
    Flux<Transaction> findAllByAccountIdAndType(UUID accountId, Type transactionType);
    Flux<Transaction> findAllByStatus(Status status);
}
