package com.testpayments.transacitonservice.integrationTest;

import com.testpayments.transacitonservice.dto.CustomerDataDto;
import com.testpayments.transacitonservice.dto.PaymentResponse;
import com.testpayments.transacitonservice.dto.TransactionResponse;
import com.testpayments.transacitonservice.dto.WithdrawalCardDataDto;
import com.testpayments.transacitonservice.dto.WithdrawalRequest;
import com.testpayments.transacitonservice.entity.Account;
import com.testpayments.transacitonservice.entity.Card;
import com.testpayments.transacitonservice.entity.Customer;
import com.testpayments.transacitonservice.entity.Merchant;
import com.testpayments.transacitonservice.entity.Status;
import com.testpayments.transacitonservice.entity.Type;
import com.testpayments.transacitonservice.repository.AccountRepository;
import com.testpayments.transacitonservice.repository.CardRepository;
import com.testpayments.transacitonservice.repository.CustomerRepository;
import com.testpayments.transacitonservice.repository.MerchantRepository;
import com.testpayments.transacitonservice.repository.TransactionRepository;
import com.testpayments.transacitonservice.repository.WebhookRepository;
import com.testpayments.transacitonservice.util.PaymentOperationConstant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PayOutControllerV1IntegrationTest extends AbstractIntegrationTest {

    private final static String MERCHANT_SECRET_KEY = "secret";

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private WebhookRepository webhookRepository;

    private UUID merchantId;

    @BeforeEach
    void beforeEach() {
        Merchant testMerchant = Merchant.builder()
                .secretKey("secret")
                .companyRecognition("Company")
                .country("USA")
                .build();

        merchantId = merchantRepository.save(testMerchant).block().getId();

        Account merhcantAccount = Account.builder()
                .currency("USD")
                .balance(new BigDecimal("1000"))
                .merchantId(testMerchant.getId())
                .build();

        accountRepository.save(merhcantAccount).block();

        Customer testCustomer = Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .country("USA")
                .build();
        customerRepository.save(testCustomer).block();

        Card testCard = Card.builder()
                .cardNumber("4111111111111111")
                .expDate(LocalDateTime.now().plusYears(3))
                .cvv("123")
                .currency("USD")
                .balance(new BigDecimal("1000.00"))
                .customerId(testCustomer.getId())
                .build();
        cardRepository.save(testCard).block();
    }

    @AfterEach
    void afterEach() {
        webhookRepository.deleteAll().block();
        transactionRepository.deleteAll().block();
        cardRepository.deleteAll().block();
        accountRepository.deleteAll().block();
        customerRepository.deleteAll().block();
        merchantRepository.deleteAll().block();
    }

    @Test
    void createWithdrawal_200() {
        WithdrawalCardDataDto withdrawalCardDataDto = new WithdrawalCardDataDto();
        withdrawalCardDataDto.setCardNumber("4111111111111111");
        WithdrawalRequest request = WithdrawalRequest.builder()
                .paymentMethod("CARD")
                .amount(100)
                .currency("USD")
                .language("en")
                .notificationUrl("https://bla")
                .customerDataDto(CustomerDataDto.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .country("USA")
                        .build())
                .withdrawalCardDataDto(withdrawalCardDataDto)
                .build();

        String base64Credentials = Base64.getEncoder().encodeToString((merchantId + ":" + MERCHANT_SECRET_KEY)
                .getBytes(StandardCharsets.UTF_8));

        EntityExchangeResult<PaymentResponse> result = webTestClient
                .post()
                .uri(PaymentOperationConstant.ROOT_URL + "/payout")
                .header("Authorization", "Basic " + base64Credentials)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .returnResult();

        assertNotNull(Objects.requireNonNull(result.getResponseBody()).getTransactionId());
        assertEquals(Status.IN_PROCESS, result.getResponseBody().getStatus());

        transactionRepository.findByIdAndType(result.getResponseBody().getTransactionId(), Type.PAY_OUT)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void createWithdrawal_404() {
        WithdrawalCardDataDto withdrawalCardDataDto = new WithdrawalCardDataDto();
        withdrawalCardDataDto.setCardNumber("4111111111111111");
        WithdrawalRequest request = WithdrawalRequest.builder()
                .paymentMethod("CARD")
                .amount(100)
                .currency("USD")
                .language("en")
                .notificationUrl("https://bla")
                .customerDataDto(CustomerDataDto.builder()
                        .firstName("Doe")
                        .lastName("Doe")
                        .country("USA")
                        .build())
                .withdrawalCardDataDto(withdrawalCardDataDto)
                .build();

        String base64Credentials = Base64.getEncoder().encodeToString((merchantId + ":" + MERCHANT_SECRET_KEY)
                .getBytes(StandardCharsets.UTF_8));

        webTestClient
                .post()
                .uri(PaymentOperationConstant.ROOT_URL + "/payout")
                .header("Authorization", "Basic " + base64Credentials)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getPayOutTransactions_200() {
        WithdrawalCardDataDto withdrawalCardDataDto = new WithdrawalCardDataDto();
        withdrawalCardDataDto.setCardNumber("4111111111111111");
        WithdrawalRequest request = WithdrawalRequest.builder()
                .paymentMethod("CARD")
                .amount(150)
                .currency("USD")
                .language("en")
                .notificationUrl("https://bla")
                .customerDataDto(CustomerDataDto.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .country("USA")
                        .build())
                .withdrawalCardDataDto(withdrawalCardDataDto)
                .build();

        String base64Credentials = Base64.getEncoder().encodeToString((merchantId + ":" + MERCHANT_SECRET_KEY)
                .getBytes(StandardCharsets.UTF_8));

        webTestClient
                .post()
                .uri(PaymentOperationConstant.ROOT_URL + "/payout")
                .header("Authorization", "Basic " + base64Credentials)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        webTestClient
                .post()
                .uri(PaymentOperationConstant.ROOT_URL + "/payout")
                .header("Authorization", "Basic " + base64Credentials)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        webTestClient
                .get()
                .uri(PaymentOperationConstant.ROOT_URL + "/payout/list")
                .header("Authorization", "Basic " + base64Credentials)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionResponse.class)
                .hasSize(2)
                .consumeWith(response -> {
                    List<TransactionResponse> transactions = response.getResponseBody();
                    assertNotNull(transactions);
                    assertTrue(transactions.stream().allMatch(t -> t.getAmount().compareTo(new BigDecimal("150")) == 0));
                    assertTrue(transactions.stream().allMatch(t -> t.getCurrency().equals("USD")));
                });
    }

    @Test
    void getPayOutTransactions_400() {
        String base64Credentials = Base64.getEncoder().encodeToString((merchantId + ":" + MERCHANT_SECRET_KEY)
                .getBytes(StandardCharsets.UTF_8));

        webTestClient
                .get()
                .uri(PaymentOperationConstant.ROOT_URL + "/payout/list")
                .header("Authorization", "Basic " + base64Credentials)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getPayOutTransactionById_200() {
        WithdrawalCardDataDto withdrawalCardDataDto = new WithdrawalCardDataDto();
        withdrawalCardDataDto.setCardNumber("4111111111111111");
        WithdrawalRequest request = WithdrawalRequest.builder()
                .paymentMethod("CARD")
                .amount(150)
                .currency("USD")
                .language("en")
                .notificationUrl("https://bla")
                .customerDataDto(CustomerDataDto.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .country("USA")
                        .build())
                .withdrawalCardDataDto(withdrawalCardDataDto)
                .build();

        String base64Credentials = Base64.getEncoder().encodeToString((merchantId + ":" + MERCHANT_SECRET_KEY)
                .getBytes(StandardCharsets.UTF_8));

        EntityExchangeResult<PaymentResponse> result = webTestClient
                .post()
                .uri(PaymentOperationConstant.ROOT_URL + "/payout")
                .header("Authorization", "Basic " + base64Credentials)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .returnResult();

        UUID transactionId = result.getResponseBody().getTransactionId();

        webTestClient
                .get()
                .uri(PaymentOperationConstant.ROOT_URL + "/payout/" + transactionId + "/details")
                .header("Authorization", "Basic " + base64Credentials)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionResponse.class)
                .consumeWith(response -> {
                    TransactionResponse transactionResponse = response.getResponseBody();
                    assertNotNull(transactionResponse);
                    assertEquals(transactionId, transactionResponse.getTransactionId());
                    assertEquals(new BigDecimal("150"), transactionResponse.getAmount());
                    assertEquals("USD", transactionResponse.getCurrency());
                    assertEquals("John", transactionResponse.getCustomerDataDto().getFirstName());
                    assertEquals("4111111111111111", transactionResponse.getTopUpCardDataDto().getCardNumber());
                });
    }

    @Test
    void getPayOutTransactionById_404() {
        String base64Credentials = Base64.getEncoder().encodeToString((merchantId + ":" + MERCHANT_SECRET_KEY)
                .getBytes(StandardCharsets.UTF_8));

        webTestClient
                .get()
                .uri(PaymentOperationConstant.ROOT_URL + "/payout/" + "634a3ee7-e2c9-4c13-a6e9-4427deae4826" + "/details")
                .header("Authorization", "Basic " + base64Credentials)
                .exchange()
                .expectStatus().isNotFound();
    }
}
