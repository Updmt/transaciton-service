package com.testpayments.transacitonservice.rest;

import com.testpayments.transacitonservice.dto.PaymentResponse;
import com.testpayments.transacitonservice.dto.TransactionResponse;
import com.testpayments.transacitonservice.dto.WithdrawalRequest;
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
public class PayOutControllerV1 {

    private final TransactionService transactionService;

    @PostMapping("/payout")
    public Mono<PaymentResponse> createWithdrawal(ServerWebExchange exchange, @RequestBody WithdrawalRequest withdrawalRequest) {
        UUID merchantId = exchange.getAttribute(ApplicationConstants.MERCHANT_ID_KEY);
        return transactionService.payOut(withdrawalRequest, merchantId);
    }

    @GetMapping("/payout/list")
    public Flux<TransactionResponse> getPayOutTransactions(ServerWebExchange exchange,
                                                           @RequestParam(value = "start_date", required = false) Long startDate,
                                                           @RequestParam(value = "end_date", required = false) Long endDate,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "5") int size) {
        UUID merchantId = exchange.getAttribute(ApplicationConstants.MERCHANT_ID_KEY);
        return transactionService.getPayOutTransactions(startDate, endDate, merchantId, page, size);
    }

    //todo Вопрос в тг
    @GetMapping("/payout/{payoutId}/details")
    public Mono<TransactionResponse> getPayOutTransactionById(@PathVariable UUID payoutId) {
        return transactionService.getPayOutTransactionById(payoutId);
    }

}
