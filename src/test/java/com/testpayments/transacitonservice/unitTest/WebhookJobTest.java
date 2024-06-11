package com.testpayments.transacitonservice.unitTest;

import com.testpayments.transacitonservice.entity.Account;
import com.testpayments.transacitonservice.entity.Card;
import com.testpayments.transacitonservice.entity.Customer;
import com.testpayments.transacitonservice.entity.Status;
import com.testpayments.transacitonservice.entity.Transaction;
import com.testpayments.transacitonservice.entity.Type;
import com.testpayments.transacitonservice.entity.Webhook;
import com.testpayments.transacitonservice.service.AccountService;
import com.testpayments.transacitonservice.service.CardService;
import com.testpayments.transacitonservice.service.CustomerService;
import com.testpayments.transacitonservice.service.TransactionService;
import com.testpayments.transacitonservice.service.webhook.WebhookJob;
import com.testpayments.transacitonservice.service.webhook.WebhookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebhookJobTest {

    @InjectMocks
    private WebhookJob webhookJob;

    @Mock
    private TransactionService transactionService;
    @Mock
    private AccountService accountService;
    @Mock
    private CardService cardService;
    @Mock
    private WebhookService webhookService;
    @Mock
    private TransactionalOperator transactionalOperator;
    @Mock
    private CustomerService customerService;

    @Test
    void assignStatusToTransactionTest() {
        Transaction transaction = new Transaction();
        transaction.setType(Type.TOP_UP);
        transaction.setAccountId(UUID.randomUUID());
        transaction.setCardId(UUID.randomUUID());
        transaction.setAmount(new BigDecimal("500"));
        transaction.setStatus(Status.APPROVED);

        Account account = new Account();
        account.setId(transaction.getAccountId());
        account.setBalance(new BigDecimal("1000"));

        Card card = new Card();
        card.setId(transaction.getCardId());
        card.setCustomerId(UUID.randomUUID());
        card.setBalance(new BigDecimal("200"));

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("Vanya");

        Webhook webhook = new Webhook();

        when(transactionService.findAllTransactionsByStatus(Status.IN_PROCESS)).thenReturn(Flux.just(transaction));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0, Mono.class));
        when(transactionService.updateTransaction(any())).thenReturn(Mono.just(transaction));
        when(transactionService.assignRandomStatus(transaction)).thenReturn(transaction);
        when(accountService.findAccountByIdForUpdate(transaction.getAccountId())).thenReturn(Mono.just(account));
        when(cardService.findCardById(transaction.getCardId())).thenReturn(Mono.just(card));
        when(accountService.updateAccount(any())).thenReturn(Mono.just(account));
        when(customerService.findCustomerById(any())).thenReturn(Mono.just(customer));
        when(webhookService.findMaxAttemptCountByTransactionId(any())).thenReturn(Mono.just(1));
        when(webhookService.sendWebhook(any(Webhook.class))).thenReturn(Mono.just("Webhook Response"));
        when(webhookService.save(any(Webhook.class))).thenReturn(Mono.just(webhook));

        StepVerifier.create(webhookJob.assignStatusToTransaction())
                .verifyComplete();

        verify(transactionService).updateTransaction(any());
        verify(accountService).findAccountByIdForUpdate(any());
        verify(cardService, times(2)).findCardById(any());
        verify(customerService).findCustomerById(any());
        verify(webhookService).sendWebhook(any(Webhook.class));
        verify(webhookService).save(any(Webhook.class));
        verify(webhookService).findMaxAttemptCountByTransactionId(any());
    }

    @Test
    void assignStatusToTransactionTest_errorInWebhookSending() {
        Transaction transaction = new Transaction();
        transaction.setType(Type.TOP_UP);
        transaction.setAccountId(UUID.randomUUID());
        transaction.setCardId(UUID.randomUUID());
        transaction.setAmount(new BigDecimal("500"));
        transaction.setStatus(Status.APPROVED);

        when(transactionService.findAllTransactionsByStatus(Status.IN_PROCESS)).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(webhookJob.assignStatusToTransaction())
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().contains("Database error"))
                .verify();
    }
}
