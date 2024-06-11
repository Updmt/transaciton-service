package com.testpayments.transacitonservice.unitTest.filter;

import com.testpayments.transacitonservice.auth.AuthService;
import com.testpayments.transacitonservice.auth.AuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationFilterTest {

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    @Mock
    private AuthService authService;

    @Mock
    private WebFilterChain webFilterChain;

    @Test
    void filter_withValidAuthorization() {
        String authHeader = "Bearer validToken";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api")
                .header("Authorization", authHeader)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(authService.authenticate(authHeader)).thenReturn(Mono.just(UUID.randomUUID()));
        when(webFilterChain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = authenticationFilter.filter(exchange, webFilterChain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(webFilterChain, times(1)).filter(exchange);
    }

    @Test
    void filter_withInvalidAuthorization() {
        String authHeader = "Bearer invalidToken";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api")
                .header("Authorization", authHeader)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(authService.authenticate(authHeader)).thenReturn(Mono.error(new RuntimeException("Invalid token")));

        Mono<Void> result = authenticationFilter.filter(exchange, webFilterChain);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();

        verify(webFilterChain, never()).filter(exchange);
        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

}
