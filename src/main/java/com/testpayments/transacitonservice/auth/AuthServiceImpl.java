package com.testpayments.transacitonservice.auth;

import com.testpayments.transacitonservice.auth.AuthService;
import com.testpayments.transacitonservice.exception.UnauthorizedException;
import com.testpayments.transacitonservice.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String BASIC_PREFIX = "Basic";
    private static final String MERCHANT_ID = "id";
    private static final String MERCHANT_SECRET_KEY = "secret";
    private static final int ZERO_ELEMENT = 0;
    private static final int FIRST_ELEMENT = 1;
    private static final int AMOUNT_ELEMENTS_SPLIT = 2;
    private static final String CREDENTIALS_SEPARATOR = ":";

    private final MerchantService merchantService;

    @Override
    public Mono<UUID> authenticate(String authenticationHeader) {
        Map<String, String> merchantIdAndSecret = retrieveMerchantIdAndSecretKey(authenticationHeader);
        UUID merchantId = UUID.fromString(merchantIdAndSecret.get(MERCHANT_ID));

        return merchantService.findByMerchantId(UUID.fromString(merchantIdAndSecret.get(MERCHANT_ID)))
                .flatMap(merchant -> Mono.just(merchant.getSecretKey()))
                .filter(el -> Objects.equals(el, merchantIdAndSecret.get(MERCHANT_SECRET_KEY)))
                .switchIfEmpty(Mono.error(new UnauthorizedException("Merchant is disabled")))
                .then(Mono.just(merchantId));
    }

    private Map<String, String> retrieveMerchantIdAndSecretKey(String authenticationHeader) {
        if (Objects.isNull(authenticationHeader)) {
            throw new RuntimeException("Auth header is null");
        }
        String base64Credentials = authenticationHeader.substring(BASIC_PREFIX.length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        String[] data = credentials.split(CREDENTIALS_SEPARATOR, AMOUNT_ELEMENTS_SPLIT);
        Map<String, String> merchantIdAndSecretKey = new HashMap<>();
        merchantIdAndSecretKey.put(MERCHANT_ID, data[ZERO_ELEMENT]);
        merchantIdAndSecretKey.put(MERCHANT_SECRET_KEY, data[FIRST_ELEMENT]);
        return merchantIdAndSecretKey;
    }
}
