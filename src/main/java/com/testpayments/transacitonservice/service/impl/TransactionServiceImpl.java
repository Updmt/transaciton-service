package com.testpayments.transacitonservice.service.impl;

import com.testpayments.transacitonservice.dto.AbstractPaymentRequest;
import com.testpayments.transacitonservice.dto.TopUpCardDataDto;
import com.testpayments.transacitonservice.dto.CustomerDataDto;
import com.testpayments.transacitonservice.dto.TopUpRequest;
import com.testpayments.transacitonservice.dto.PaymentResponse;
import com.testpayments.transacitonservice.dto.TransactionResponse;
import com.testpayments.transacitonservice.dto.WithdrawalRequest;
import com.testpayments.transacitonservice.entity.Account;
import com.testpayments.transacitonservice.entity.Card;
import com.testpayments.transacitonservice.entity.Customer;
import com.testpayments.transacitonservice.entity.Status;
import com.testpayments.transacitonservice.entity.Transaction;
import com.testpayments.transacitonservice.entity.Type;
import com.testpayments.transacitonservice.exception.CustomNotFoundException;
import com.testpayments.transacitonservice.exception.InsufficientFundsException;
import com.testpayments.transacitonservice.repository.TransactionRepository;
import com.testpayments.transacitonservice.service.AccountService;
import com.testpayments.transacitonservice.service.CardService;
import com.testpayments.transacitonservice.service.CustomerService;
import com.testpayments.transacitonservice.service.TransactionService;
import com.testpayments.transacitonservice.util.DateConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CustomerService customerService;
    private final CardService cardService;


    @Override
    public Mono<PaymentResponse> topUp(TopUpRequest topUpRequest, UUID merchantId) {
        return accountService.findAccountByMerchantIdAndCurrency(merchantId, topUpRequest.getCurrency())
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Merchant with this id and currency does not exist")))
                .flatMap(account -> {
                    CustomerDataDto customerDR = topUpRequest.getCustomerDataDto();
                    return customerService.findCustomerByFirstNameAndLastNameAndCountry(customerDR.getFirstName(), customerDR.getLastName(), customerDR.getCountry())
                            .flatMap(customer -> {
                                TopUpCardDataDto cardDR = topUpRequest.getTopUpCardDataDto();
                                return cardService.findCardByCardNumberAndCurrency(cardDR.getCardNumber(), topUpRequest.getCurrency())
                                        .flatMap(card -> reduceCardBalanceAndCreateTransaction(card, topUpRequest, account.getId()))
                                        .switchIfEmpty(Mono.defer(() -> createCardAndMap(topUpRequest, customer.getId())
                                                .flatMap(card -> Mono.error(new InsufficientFundsException("Not enough money on balance")))));
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                Customer newCustomer = mapCustomerRequest(topUpRequest.getCustomerDataDto());
                                return customerService.createCustomer(newCustomer)
                                        .flatMap(customer -> createCardAndMap(topUpRequest, customer.getId())
                                                .flatMap(card -> Mono.error(new InsufficientFundsException("Not enough money on balance"))));
                            }));
                })
                .map(transaction -> PaymentResponse.builder()
                        .transactionId(transaction.getId())
                        .status(transaction.getStatus())
                        .message("OK")
                        .build());
    }

    @Override
    public Mono<PaymentResponse> payOut(WithdrawalRequest withdrawalRequest, UUID merchantId) {
        CustomerDataDto customerDR = withdrawalRequest.getCustomerDataDto();
        return customerService.findCustomerByFirstNameAndLastNameAndCountry(
                        customerDR.getFirstName(), customerDR.getLastName(), customerDR.getCountry()
                )
                .switchIfEmpty(Mono.error(new CustomNotFoundException("There is no such customer")))
                .flatMap(customer -> cardService.findCardByCardNumberAndCurrency(
                        withdrawalRequest.getWithdrawalCardDataDto().getCardNumber(), withdrawalRequest.getCurrency()
                ))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("There is no such card")))
                .flatMap(card -> accountService.findAccountByMerchantIdAndCurrency(merchantId, withdrawalRequest.getCurrency())
                        .flatMap(account -> reduceAccountBalanceAndCreateTransaction(account, withdrawalRequest, card.getId())))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Merchant with this id and currency does not exist")))
                .map(transaction -> PaymentResponse.builder()
                        .transactionId(transaction.getId())
                        .status(transaction.getStatus())
                        .message("OK")
                        .build());
    }

    @Override
    public Mono<Transaction> createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    //ищем все аккаунты мерчанта, ищем все транзакции для конкретного мерчанта
    //сортировки никакой нет
    @Override
    public Flux<TransactionResponse> getTopUpTransactions(Long firstDate, Long lastDate, UUID merchantId, int page, int size) {
        long offset = (long) page * size;
        return accountService.findAccountsByMerchantId(merchantId)
                .flatMap(account -> {
                    if (Objects.nonNull(firstDate) && Objects.nonNull(lastDate)) {
                        LocalDateTime startDate = DateConverter.convertUnixTimestampToLocalDateTime(firstDate);
                        LocalDateTime endDate = DateConverter.convertUnixTimestampToLocalDateTime(lastDate);
                        return transactionRepository.findByDateRangeAndType(startDate, endDate, account.getId(), Type.TOP_UP, size, offset)
                                .flatMap(this::findAndMapByCardIdAndCustomerId);
                    } else {
                        return transactionRepository.findAllByAccountIdAndType(account.getId(), Type.TOP_UP)
                                .flatMap(this::findAndMapByCardIdAndCustomerId);
                    }
                })
                .switchIfEmpty(Mono.error(new CustomNotFoundException("No transactions were found")));
    }

    @Override
    public Flux<TransactionResponse> getPayOutTransactions(Long firstDate, Long lastDate, UUID merchantId, int page, int size) {
        long offset = (long) page * size;
        return accountService.findAccountsByMerchantId(merchantId)
                .flatMap(account -> {
                    if (Objects.nonNull(firstDate) && Objects.nonNull(lastDate)) {
                        LocalDateTime startDate = DateConverter.convertUnixTimestampToLocalDateTime(firstDate);
                        LocalDateTime endDate = DateConverter.convertUnixTimestampToLocalDateTime(lastDate);
                        return transactionRepository.findByDateRangeAndType(startDate, endDate, account.getId(), Type.PAY_OUT, size, offset)
                                .flatMap(this::findAndMapByCardIdAndCustomerId);
                    } else {
                        return transactionRepository.findAllByAccountIdAndType(account.getId(), Type.PAY_OUT)
                                .flatMap(this::findAndMapByCardIdAndCustomerId);
                    }
                })
                .switchIfEmpty(Mono.error(new CustomNotFoundException("No transactions were found")));
    }

    @Override
    public Mono<TransactionResponse> getTopUpTransactionById(UUID transactionId, UUID merchantId) {
        return transactionRepository.findByIdAndType(transactionId, Type.TOP_UP)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Transaction was not found")))
                .flatMap(transaction ->
                        accountService.findAccountByMerchantIdAndCurrency(merchantId, transaction.getCurrency())
                                .switchIfEmpty(Mono.error(new CustomNotFoundException("Account with this currency and merchantId was not found")))
                                .flatMap(account -> findAndMapByCardIdAndCustomerId(transaction))
                );
    }

    @Override
    public Mono<TransactionResponse> getPayOutTransactionById(UUID transactionId, UUID merchantId) {
        return transactionRepository.findByIdAndType(transactionId, Type.PAY_OUT)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Transaction was not found")))
                .flatMap(transaction ->
                        accountService.findAccountByMerchantIdAndCurrency(merchantId, transaction.getCurrency())
                                .switchIfEmpty(Mono.error(new CustomNotFoundException("Account with this currency and merchantId was not found")))
                                .flatMap(account -> findAndMapByCardIdAndCustomerId(transaction))
                );
    }

    @Override
    public Flux<Transaction> findAllTransactionsByStatus(Status status) {
        return transactionRepository.findAllByStatus(status);
    }

    @Override
    public Mono<Transaction> updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    private Mono<Card> createCardAndMap(TopUpRequest topUpRequest, UUID customerId) {
        Card newCard = mapCardRequest(topUpRequest.getTopUpCardDataDto());
        newCard.setCurrency(topUpRequest.getCurrency());
        newCard.setCustomerId(customerId);
        return cardService.createCard(newCard);
    }


    public Mono<Transaction> reduceCardBalanceAndCreateTransaction(Card card, TopUpRequest topUpRequest, UUID accountId) {
        return cardService.updateCardBalance(card, topUpRequest)
                .flatMap(updatedCard -> createTransactionAfterBalanceUpdate(topUpRequest, card.getId(), accountId));
    }

    private Mono<Transaction> createTransactionAfterBalanceUpdate(TopUpRequest topUpRequest, UUID cardId, UUID accountId) {
        Transaction transaction = mapTransaction(topUpRequest, cardId, accountId);
        transaction.setType(Type.TOP_UP);
        return createTransaction(transaction);
    }

    public Mono<Transaction> reduceAccountBalanceAndCreateTransaction(Account account, WithdrawalRequest withdrawalRequest, UUID cardId) {
        return accountService.updateAccountBalance(account, withdrawalRequest)
                .flatMap(updatedAccount -> createTransactionAfterBalanceUpdate(withdrawalRequest, cardId, account.getId()));
    }

    private Mono<Transaction> createTransactionAfterBalanceUpdate(WithdrawalRequest withdrawalRequest, UUID cardId, UUID accountId) {
        Transaction transaction = mapTransaction(withdrawalRequest, cardId, accountId);
        transaction.setType(Type.PAY_OUT);
        return createTransaction(transaction);
    }

    private Mono<TransactionResponse> findAndMapByCardIdAndCustomerId(Transaction transaction) {
        return cardService.findCardById(transaction.getCardId())
                .flatMap(card -> {
                    UUID customerId = card.getCustomerId();
                    return customerService.findCustomerById(customerId)
                            .map(customer -> {
                                CustomerDataDto customerDataDto = new CustomerDataDto(customer.getFirstName(), customer.getLastName(), customer.getCountry());
                                TopUpCardDataDto topUpCardDataDto = new TopUpCardDataDto();
                                topUpCardDataDto.setCardNumber(card.getCardNumber());
                                return mapTransactionToResponse(transaction, topUpCardDataDto, customerDataDto);
                            });
                });
    }


    private TransactionResponse mapTransactionToResponse(Transaction transaction, TopUpCardDataDto topUpCardDataDto,
                                                         CustomerDataDto customerDataDto) {
        return TransactionResponse.builder()
                .paymentMethod("CARD")
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionId(transaction.getId())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .notificationUrl(transaction.getNotificationUrl())
                .topUpCardDataDto(topUpCardDataDto)
                .language(transaction.getLanguage())
                .customerDataDto(customerDataDto)
                .message("OK")
                .build();

    }

    private Transaction mapTransaction(AbstractPaymentRequest paymentRequest, UUID cardId, UUID accountId) {
        return Transaction.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .currency(paymentRequest.getCurrency())
                .amount(BigDecimal.valueOf(paymentRequest.getAmount().longValue()))
                .notificationUrl(paymentRequest.getNotificationUrl())
                .language(paymentRequest.getLanguage())
                .status(Status.IN_PROCESS)
                .cardId(cardId)
                .accountId(accountId)
                .build();

    }

    private Customer mapCustomerRequest(CustomerDataDto customerDataDto) {
        return Customer.builder()
                .firstName(customerDataDto.getFirstName())
                .lastName(customerDataDto.getLastName())
                .country(customerDataDto.getCountry())
                .build();
    }

    private Card mapCardRequest(TopUpCardDataDto topUpCardDataDto) {
        return Card.builder()
                .cardNumber(topUpCardDataDto.getCardNumber())
                .cvv(topUpCardDataDto.getSvv())
                .expDate(DateConverter.convertStringToLocalDateTime(topUpCardDataDto.getExpDate()))
                .build();

    }
}
