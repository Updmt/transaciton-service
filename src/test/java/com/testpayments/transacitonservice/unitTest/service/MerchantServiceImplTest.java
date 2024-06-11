package com.testpayments.transacitonservice.unitTest.service;

import com.testpayments.transacitonservice.entity.Merchant;
import com.testpayments.transacitonservice.exception.CustomNotFoundException;
import com.testpayments.transacitonservice.repository.MerchantRepository;
import com.testpayments.transacitonservice.service.impl.MerchantServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MerchantServiceImplTest {

    @InjectMocks
    private MerchantServiceImpl merchantService;

    @Mock
    private MerchantRepository merchantRepository;

    @Test
    void findByMerchantId_ok() {
        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setCompanyRecognition("Company Inc.");

        when(merchantRepository.findById(merchantId)).thenReturn(Mono.just(merchant));

        Mono<Merchant> result = merchantService.findByMerchantId(merchantId);

        StepVerifier.create(result)
                .expectNextMatches(foundMerchant -> foundMerchant.getId().equals(merchantId) &&
                        foundMerchant.getCompanyRecognition().equals("Company Inc."))
                .verifyComplete();
    }

    @Test
    void findByMerchantId_throwException() {
        UUID merchantId = UUID.randomUUID();

        when(merchantRepository.findById(merchantId)).thenReturn(Mono.empty());

        Mono<Merchant> result = merchantService.findByMerchantId(merchantId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundException &&
                        throwable.getMessage().equals("Merchant not found"))
                .verify();
    }
}
