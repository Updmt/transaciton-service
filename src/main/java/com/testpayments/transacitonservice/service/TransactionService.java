package com.testpayments.transacitonservice.service;

import com.testpayments.transacitonservice.dto.TopUpRequest;
import com.testpayments.transacitonservice.dto.PaymentResponse;
import com.testpayments.transacitonservice.dto.TransactionResponse;
import com.testpayments.transacitonservice.dto.WithdrawalRequest;
import com.testpayments.transacitonservice.entity.Status;
import com.testpayments.transacitonservice.entity.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TransactionService {

    Mono<PaymentResponse> topUp(TopUpRequest topUpRequest, UUID id);
    Mono<PaymentResponse> payOut(WithdrawalRequest withdrawalRequest, UUID id);
    Mono<Transaction> createTransaction(Transaction transaction);
    Flux<TransactionResponse> getTopUpTransactions(Long startDate, Long endDate, UUID merchantId, int page, int size);
    Flux<TransactionResponse> getPayOutTransactions(Long startDate, Long endDate, UUID merchantId, int page, int size);
    Mono<TransactionResponse> getTopUpTransactionById(UUID id);
    Mono<TransactionResponse> getPayOutTransactionById(UUID id);
    Flux<Transaction> findAllTransactionsByStatus(Status status);
    Mono<Transaction> updateTransaction(Transaction transaction);
}
