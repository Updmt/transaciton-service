package com.testpayments.transacitonservice.auth;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AuthService {

    Mono<UUID> authenticate(String authenticationHeader);
}
