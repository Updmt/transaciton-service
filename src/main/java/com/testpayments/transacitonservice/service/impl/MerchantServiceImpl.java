package com.testpayments.transacitonservice.service.impl;

import com.testpayments.transacitonservice.entity.Merchant;
import com.testpayments.transacitonservice.exception.CustomNotFoundException;
import com.testpayments.transacitonservice.repository.MerchantRepository;
import com.testpayments.transacitonservice.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;

    @Override
    public Mono<Merchant> findByMerchantId(UUID id) {
        return merchantRepository.findById(id)
                .doOnSuccess(u -> log.info("Merchant with id {} was found", id))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Merchant not found")));
    }
}
