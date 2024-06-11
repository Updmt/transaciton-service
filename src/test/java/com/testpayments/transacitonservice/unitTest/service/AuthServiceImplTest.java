package com.testpayments.transacitonservice.unitTest.service;

import com.testpayments.transacitonservice.auth.AuthServiceImpl;
import com.testpayments.transacitonservice.entity.Merchant;
import com.testpayments.transacitonservice.exception.UnauthorizedException;
import com.testpayments.transacitonservice.service.MerchantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private MerchantService merchantService;

    private static final String VALID_AUTH_HEADER = "Basic " + Base64.getEncoder()
            .encodeToString("b35f70de-d0a5-4c1a-a765-ccb3b6be33ac:secretKey".getBytes(StandardCharsets.UTF_8));

    private static final String INVALID_AUTH_HEADER = "Basic " + Base64.getEncoder()
            .encodeToString("b35f70de-d0a5-4c1a-a765-ccb3b6be33ac:invalidKey".getBytes(StandardCharsets.UTF_8));

    private static final UUID VALID_MERCHANT_ID = UUID.fromString("b35f70de-d0a5-4c1a-a765-ccb3b6be33ac");
    private static final String VALID_SECRET = "secretKey";

    @Test
    void authenticate_ok() {
        Merchant merchant = new Merchant();
        merchant.setId(VALID_MERCHANT_ID);
        merchant.setSecretKey(VALID_SECRET);

        when(merchantService.findByMerchantId(any(UUID.class))).thenReturn(Mono.just(merchant));

        Mono<UUID> result = authService.authenticate(VALID_AUTH_HEADER);

        StepVerifier.create(result)
                .expectNextMatches(uuid -> uuid.equals(VALID_MERCHANT_ID))
                .verifyComplete();
    }

    @Test
    void authenticate_throwException() {
        Merchant merchant = new Merchant();
        merchant.setId(VALID_MERCHANT_ID);
        merchant.setSecretKey(VALID_SECRET);

        when(merchantService.findByMerchantId(any(UUID.class))).thenReturn(Mono.just(merchant));

        Mono<UUID> result = authService.authenticate(INVALID_AUTH_HEADER);

        StepVerifier.create(result)
                .expectError(UnauthorizedException.class)
                .verify();
    }
}
