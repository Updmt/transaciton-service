package com.testpayments.transacitonservice.service;

import com.testpayments.transacitonservice.entity.Merchant;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MerchantService {

    Mono<Merchant> findByMerchantId(UUID id);
}
