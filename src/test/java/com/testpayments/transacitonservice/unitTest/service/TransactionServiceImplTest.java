package com.testpayments.transacitonservice.unitTest.service;

import com.testpayments.transacitonservice.dto.CustomerDataDto;
import com.testpayments.transacitonservice.dto.PaymentResponse;
import com.testpayments.transacitonservice.dto.TopUpCardDataDto;
import com.testpayments.transacitonservice.dto.TopUpRequest;
import com.testpayments.transacitonservice.dto.TransactionResponse;
import com.testpayments.transacitonservice.dto.WithdrawalCardDataDto;
import com.testpayments.transacitonservice.dto.WithdrawalRequest;
import com.testpayments.transacitonservice.entity.Account;
import com.testpayments.transacitonservice.entity.Card;
import com.testpayments.transacitonservice.entity.Customer;
import com.testpayments.transacitonservice.entity.Status;
import com.testpayments.transacitonservice.entity.Transaction;
import com.testpayments.transacitonservice.entity.Type;
import com.testpayments.transacitonservice.exception.CustomNotFoundException;
import com.testpayments.transacitonservice.repository.TransactionRepository;
import com.testpayments.transacitonservice.service.AccountService;
import com.testpayments.transacitonservice.service.CardService;
import com.testpayments.transacitonservice.service.CustomerService;
import com.testpayments.transacitonservice.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private CustomerService customerService;
    @Mock
    private CardService cardService;

    @Test
    void topUp_ok() {
        UUID merchantId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        String cardNumber = "1234567890123456";
        String cvv = "123";
        String currency = "USD";

        CustomerDataDto customerData = CustomerDataDto.builder()
                .firstName("John")
                .lastName("Doe")
                .country("USA")
                .build();

        TopUpCardDataDto cardData = TopUpCardDataDto.builder()
                .cardNumber(cardNumber)
                .svv(cvv)
                .expDate("12/2023")
                .build();

        TopUpRequest topUpRequest = new TopUpRequest();
        topUpRequest.setPaymentMethod("credit_card");
        topUpRequest.setAmount(100);
        topUpRequest.setCurrency(currency);
        topUpRequest.setLanguage("en");
        topUpRequest.setNotificationUrl("https://example.com/notify");
        topUpRequest.setCustomerDataDto(customerData);
        topUpRequest.setTopUpCardDataDto(cardData);

        Account account = new Account();
        account.setId(accountId);
        account.setMerchantId(merchantId);
        account.setCurrency(currency);

        Customer customer = new Customer(customerId, "John", "Doe", "USA");

        Card card = new Card(cardId, cardNumber, LocalDateTime.now(), cvv, currency, new BigDecimal("1000.00"), customerId);

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setCardId(cardId);
        transaction.setAccountId(accountId);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setStatus(Status.IN_PROCESS);

        when(accountService.findAccountByMerchantIdAndCurrency(merchantId, currency)).thenReturn(Mono.just(account));
        when(customerService.findCustomerByFirstNameAndLastNameAndCountry("John", "Doe", "USA")).thenReturn(Mono.just(customer));
        when(cardService.findCardByCardNumberAndCurrency(cardNumber, currency)).thenReturn(Mono.just(card));
        when(cardService.updateCardBalance(card, topUpRequest)).thenReturn(Mono.just(card));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(Mono.just(transaction));

        Mono<PaymentResponse> result = transactionService.topUp(topUpRequest, merchantId);

        StepVerifier.create(result)
                .expectNextMatches(paymentResponse -> paymentResponse.getStatus().equals(Status.IN_PROCESS) &&
                        paymentResponse.getTransactionId().equals(transaction.getId()))
                .verifyComplete();
    }

    @Test
    void topUp_throwException() {
        UUID merchantId = UUID.randomUUID();
        TopUpRequest topUpRequest = mock(TopUpRequest.class);

        when(topUpRequest.getCurrency()).thenReturn("USD");
        when(accountService.findAccountByMerchantIdAndCurrency(merchantId, "USD")).thenReturn(Mono.empty());

        Mono<PaymentResponse> result = transactionService.topUp(topUpRequest, merchantId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundException &&
                        throwable.getMessage().equals("Merchant with this id and currency does not exist"))
                .verify();
    }

    @Test
    void payOut_ok() {
        UUID merchantId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        String cardNumber = "1234567890123456";
        String cvv = "123";
        String currency = "USD";

        CustomerDataDto customerData = CustomerDataDto.builder()
                .firstName("John")
                .lastName("Doe")
                .country("USA")
                .build();

        WithdrawalCardDataDto withdrawalCardDataDto = new WithdrawalCardDataDto();
        withdrawalCardDataDto.setCardNumber(cardNumber);

        WithdrawalRequest withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setWithdrawalCardDataDto(withdrawalCardDataDto);
        withdrawalRequest.setPaymentMethod("credit_card");
        withdrawalRequest.setAmount(100);
        withdrawalRequest.setCurrency(currency);
        withdrawalRequest.setLanguage("en");
        withdrawalRequest.setNotificationUrl("https://example.com/notify");
        withdrawalRequest.setCustomerDataDto(customerData);

        Account account = new Account();
        account.setId(accountId);
        account.setMerchantId(merchantId);
        account.setCurrency(currency);

        Customer customer = new Customer(customerId, "John", "Doe", "USA");

        Card card = new Card(cardId, cardNumber, LocalDateTime.now(), cvv, currency, new BigDecimal("1000.00"), customerId);

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setCardId(cardId);
        transaction.setAccountId(accountId);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setStatus(Status.IN_PROCESS);

        when(customerService.findCustomerByFirstNameAndLastNameAndCountry("John", "Doe", "USA")).thenReturn(Mono.just(customer));
        when(cardService.findCardByCardNumberAndCurrency(cardNumber, currency)).thenReturn(Mono.just(card));
        when(accountService.findAccountByMerchantIdAndCurrency(merchantId, currency)).thenReturn(Mono.just(account));
        when(accountService.updateAccountBalance(account, withdrawalRequest)).thenReturn(Mono.just(account));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(Mono.just(transaction));

        Mono<PaymentResponse> result = transactionService.payOut(withdrawalRequest, merchantId);

        StepVerifier.create(result)
                .expectNextMatches(paymentResponse ->
                        paymentResponse.getTransactionId().equals(transaction.getId()) &&
                                paymentResponse.getStatus().equals(Status.IN_PROCESS))
                .verifyComplete();
    }

    @Test
    void payOut_throwException() {
        UUID merchantId = UUID.randomUUID();

        CustomerDataDto customerData = new CustomerDataDto("John", "Doe", "USA");
        WithdrawalRequest withdrawalRequest = mock(WithdrawalRequest.class);

        when(withdrawalRequest.getCustomerDataDto()).thenReturn(customerData);
        when(customerService.findCustomerByFirstNameAndLastNameAndCountry("John", "Doe", "USA")).thenReturn(Mono.empty());

        Mono<PaymentResponse> result = transactionService.payOut(withdrawalRequest, merchantId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundException &&
                        throwable.getMessage().equals("There is no such customer"))
                .verify();
    }

    @Test
    void createTransaction_ok() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAmount(new BigDecimal("100.00"));

        when(transactionRepository.save(transaction)).thenReturn(Mono.just(transaction));

        Mono<Transaction> transactionMono = transactionService.createTransaction(transaction);

        StepVerifier.create(transactionMono)
                .expectNextMatches(savedTransaction ->
                        savedTransaction.getId().equals(transaction.getId()) &&
                                savedTransaction.getAmount().compareTo(new BigDecimal("100.00")) == 0)
                .verifyComplete();
    }

    @Test
    void createTransaction_throwException() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAmount(new BigDecimal("100.00"));
        RuntimeException expectedException = new RuntimeException("Database error");

        when(transactionRepository.save(transaction)).thenReturn(Mono.error(expectedException));

        Mono<Transaction> transactionMono = transactionService.createTransaction(transaction);

        StepVerifier.create(transactionMono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getTopUpTransactions_ok() {
        Long firstDate = 1609459200L;
        Long lastDate = 1640995200L;
        int page = 0;
        int size = 10;
        UUID merchantId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String currency = "USD";

        Account account = new Account();
        account.setId(accountId);
        account.setMerchantId(merchantId);
        account.setCurrency(currency);

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(accountId);
        transaction.setCardId(cardId);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency(currency);
        transaction.setType(Type.TOP_UP);

        Card card = new Card(cardId, "1234567890123456", LocalDateTime.now(), "123", "USD", new BigDecimal("1000.00"), customerId);
        Customer customer = new Customer(customerId, "John", "Doe", currency);

        when(accountService.findAccountsByMerchantId(merchantId)).thenReturn(Flux.just(account));
        when(transactionRepository.findByDateRangeAndType(any(LocalDateTime.class), any(LocalDateTime.class), eq(accountId), eq(Type.TOP_UP), eq(size), eq((long) page * size)))
                .thenReturn(Flux.just(transaction));
        when(cardService.findCardById(cardId)).thenReturn(Mono.just(card));
        when(customerService.findCustomerById(customerId)).thenReturn(Mono.just(customer));

        Flux<TransactionResponse> result = transactionService.getTopUpTransactions(firstDate, lastDate, merchantId, page, size);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTransactionId().equals(transaction.getId()) &&
                        response.getAmount().equals(transaction.getAmount()) &&
                        response.getCustomerDataDto().getFirstName().equals("John"))
                .verifyComplete();
    }

    @Test
    void getTopUpTransactions_ThrowException() {
        Long firstDate = 1609459200L;
        Long lastDate = 1640995200L;
        UUID merchantId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        when(accountService.findAccountsByMerchantId(merchantId)).thenReturn(Flux.empty());

        Flux<TransactionResponse> result = transactionService.getTopUpTransactions(firstDate, lastDate, merchantId, page, size);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundException &&
                        throwable.getMessage().equals("No transactions were found"))
                .verify();
    }

    @Test
    void getPayOutTransactions_ok() {
        Long firstDate = 1609459200L;
        Long lastDate = 1640995200L;
        int page = 0;
        int size = 10;
        UUID merchantId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String currency = "USD";

        Account account = new Account();
        account.setId(accountId);
        account.setMerchantId(merchantId);
        account.setCurrency(currency);

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(accountId);
        transaction.setCardId(cardId);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency(currency);
        transaction.setType(Type.PAY_OUT);

        Card card = new Card(cardId, "1234567890123456", LocalDateTime.now(), "123", "USD", new BigDecimal("1000.00"), customerId);
        Customer customer = new Customer(customerId, "John", "Doe", currency);

        when(accountService.findAccountsByMerchantId(merchantId)).thenReturn(Flux.just(account));
        when(transactionRepository.findByDateRangeAndType(any(LocalDateTime.class), any(LocalDateTime.class), eq(accountId), eq(Type.PAY_OUT), eq(size), eq((long) page * size)))
                .thenReturn(Flux.just(transaction));
        when(cardService.findCardById(cardId)).thenReturn(Mono.just(card));
        when(customerService.findCustomerById(customerId)).thenReturn(Mono.just(customer));

        Flux<TransactionResponse> result = transactionService.getPayOutTransactions(firstDate, lastDate, merchantId, page, size);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTransactionId().equals(transaction.getId()) &&
                        response.getAmount().equals(transaction.getAmount()) &&
                        response.getCustomerDataDto().getFirstName().equals("John"))
                .verifyComplete();
    }

    @Test
    void getPayOutTransactions_throwException() {
        Long firstDate = 1609459200L;
        Long lastDate = 1640995200L;
        UUID merchantId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        when(accountService.findAccountsByMerchantId(merchantId)).thenReturn(Flux.empty());

        Flux<TransactionResponse> result = transactionService.getPayOutTransactions(firstDate, lastDate, merchantId, page, size);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundException &&
                        throwable.getMessage().equals("No transactions were found"))
                .verify();
    }

    @Test
    void getTopUpTransactionById_Successful() {
        UUID transactionId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String currency = "USD";

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setCardId(cardId);
        transaction.setCurrency(currency);
        transaction.setAmount(new BigDecimal("100.00"));

        Account account = new Account();
        Card card = new Card(cardId, "1234567890123456", null, "123", currency, new BigDecimal("1000.00"), customerId);
        Customer customer = new Customer(customerId, "John", "Doe", "USA");

        TopUpCardDataDto topUpCardDataDto = new TopUpCardDataDto();
        topUpCardDataDto.setCardNumber("1234567890123456");

        TransactionResponse expectedResponse = new TransactionResponse();
        expectedResponse.setTransactionId(transactionId);
        expectedResponse.setAmount(new BigDecimal("100.00"));
        expectedResponse.setCurrency(currency);
        expectedResponse.setCustomerDataDto(new CustomerDataDto("John", "Doe", "USA"));
        expectedResponse.setTopUpCardDataDto(topUpCardDataDto);

        when(transactionRepository.findByIdAndType(transactionId, Type.TOP_UP)).thenReturn(Mono.just(transaction));
        when(accountService.findAccountByMerchantIdAndCurrency(merchantId, currency)).thenReturn(Mono.just(account));
        when(cardService.findCardById(cardId)).thenReturn(Mono.just(card));
        when(customerService.findCustomerById(customerId)).thenReturn(Mono.just(customer));

        Mono<TransactionResponse> result = transactionService.getTopUpTransactionById(transactionId, merchantId);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getTransactionId().equals(transactionId) &&
                                response.getAmount().equals(new BigDecimal("100.00")) &&
                                response.getCurrency().equals(currency) &&
                                response.getCustomerDataDto().getFirstName().equals("John")
                )
                .verifyComplete();
    }

    @Test
    void getTopUpTransactionById_throwException() {
        UUID transactionId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        when(transactionRepository.findByIdAndType(transactionId, Type.TOP_UP)).thenReturn(Mono.empty());

        Mono<TransactionResponse> result = transactionService.getTopUpTransactionById(transactionId, merchantId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundException &&
                        throwable.getMessage().equals("Transaction was not found"))
                .verify();
    }

    @Test
    void getPayOutTransactionById_ok() {
        UUID transactionId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String currency = "USD";

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setCardId(cardId);
        transaction.setCurrency(currency);
        transaction.setAmount(new BigDecimal("100.00"));

        Account account = new Account();
        Card card = new Card(cardId, "1234567890123456", null, "123", currency, new BigDecimal("1000.00"), customerId);
        Customer customer = new Customer(customerId, "John", "Doe", "USA");

        TopUpCardDataDto topUpCardDataDto = new TopUpCardDataDto();
        topUpCardDataDto.setCardNumber("1234567890123456");

        TransactionResponse expectedResponse = new TransactionResponse();
        expectedResponse.setTransactionId(transactionId);
        expectedResponse.setAmount(new BigDecimal("100.00"));
        expectedResponse.setCurrency(currency);
        expectedResponse.setCustomerDataDto(new CustomerDataDto("John", "Doe", "USA"));
        expectedResponse.setTopUpCardDataDto(topUpCardDataDto);

        when(transactionRepository.findByIdAndType(transactionId, Type.PAY_OUT)).thenReturn(Mono.just(transaction));
        when(accountService.findAccountByMerchantIdAndCurrency(merchantId, currency)).thenReturn(Mono.just(account));
        when(cardService.findCardById(cardId)).thenReturn(Mono.just(card));
        when(customerService.findCustomerById(customerId)).thenReturn(Mono.just(customer));

        Mono<TransactionResponse> result = transactionService.getPayOutTransactionById(transactionId, merchantId);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getTransactionId().equals(transactionId) &&
                                response.getAmount().equals(new BigDecimal("100.00")) &&
                                response.getCurrency().equals(currency) &&
                                response.getCustomerDataDto().getFirstName().equals("John")
                )
                .verifyComplete();
    }

    @Test
    void getPayOutTransactionById_throwException() {
        UUID transactionId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        when(transactionRepository.findByIdAndType(transactionId, Type.PAY_OUT)).thenReturn(Mono.empty());

        Mono<TransactionResponse> result = transactionService.getPayOutTransactionById(transactionId, merchantId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundException &&
                        throwable.getMessage().equals("Transaction was not found"))
                .verify();
    }

    @Test
    void findAllTransactionsByStatus_ok() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setStatus(Status.IN_PROCESS);

        when(transactionRepository.findAllByStatus(Status.IN_PROCESS)).thenReturn(Flux.just(transaction));

        Flux<Transaction> result = transactionService.findAllTransactionsByStatus(Status.IN_PROCESS);

        StepVerifier.create(result)
                .expectNextMatches(foundTransaction -> foundTransaction.getStatus() == Status.IN_PROCESS)
                .verifyComplete();
    }

    @Test
    void findAllTransactionsByStatus_throwException() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setStatus(Status.IN_PROCESS);
        RuntimeException expectedException = new RuntimeException("Database error");

        when(transactionRepository.findAllByStatus(Status.IN_PROCESS)).thenReturn(Flux.error(expectedException));

        Flux<Transaction> result = transactionService.findAllTransactionsByStatus(Status.IN_PROCESS);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void updateTransaction_ok() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setStatus(Status.IN_PROCESS);

        when(transactionRepository.save(transaction)).thenReturn(Mono.just(transaction));

        Mono<Transaction> result = transactionService.updateTransaction(transaction);

        StepVerifier.create(result)
                .expectNextMatches(savedTransaction -> savedTransaction.getStatus() == Status.IN_PROCESS)
                .verifyComplete();
    }

    @Test
    void updateTransaction_throwException() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setStatus(Status.IN_PROCESS);

        when(transactionRepository.save(transaction)).thenReturn(Mono.error(new RuntimeException("Database failure")));

        Mono<Transaction> result = transactionService.updateTransaction(transaction);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Database failure"))
                .verify();
    }
}
