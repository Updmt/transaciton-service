package com.testpayments.transacitonservice.rest;

import com.testpayments.transacitonservice.dto.TopUpRequest;
import com.testpayments.transacitonservice.dto.PaymentResponse;
import com.testpayments.transacitonservice.dto.TransactionResponse;
import com.testpayments.transacitonservice.service.TransactionService;
import com.testpayments.transacitonservice.util.ApplicationConstants;
import com.testpayments.transacitonservice.util.PaymentOperationConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(PaymentOperationConstant.ROOT_URL)
public class TopUpControllerV1 {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public Mono<PaymentResponse> createTransaction(ServerWebExchange exchange, @RequestBody TopUpRequest topUpRequest) {
        UUID merchantId = exchange.getAttribute(ApplicationConstants.MERCHANT_ID_KEY);
        return transactionService.topUp(topUpRequest, merchantId);
    }

    @GetMapping("/transaction/list")
    public Flux<TransactionResponse> getTopUpTransactions(ServerWebExchange exchange,
                                                          @RequestParam(value = "start_date", required = false) Long startDate,
                                                          @RequestParam(value = "end_date", required = false) Long endDate,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "5") int size) {
        UUID merchantId = exchange.getAttribute(ApplicationConstants.MERCHANT_ID_KEY);
        return transactionService.getTopUpTransactions(startDate, endDate, merchantId, page, size);
    }

    @GetMapping("/transaction/{transactionId}/details")
    public Mono<TransactionResponse> getTopUpTransactionById(ServerWebExchange exchange, @PathVariable UUID transactionId) {
        UUID merchantId = exchange.getAttribute(ApplicationConstants.MERCHANT_ID_KEY);
        return transactionService.getTopUpTransactionById(transactionId, merchantId);
    }
}
