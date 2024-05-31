package com.testpayments.transacitonservice.exception.handler;

import com.testpayments.transacitonservice.exception.CustomNotFoundException;
import com.testpayments.transacitonservice.exception.ErrorResponse;
import com.testpayments.transacitonservice.exception.InsufficientFundsException;
import com.testpayments.transacitonservice.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCustomNotFoundException(CustomNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("FAILED", "PAYMENT_METHOD_NOT_ALLOWED");
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<String>> handleRuntimeException(RuntimeException ex) {
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ex.getMessage())
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Mono<ResponseEntity<String>> handleRuntimeException(UnauthorizedException ex) {
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ex.getMessage())
        );
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCustomNotFoundException(InsufficientFundsException ex) {
        ErrorResponse errorResponse = new ErrorResponse("FAILED", "PAYMENT_METHOD_NOT_ALLOWED");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }
}
