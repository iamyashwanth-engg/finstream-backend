package com.banking.finstream.finstream_backend.controller;

import com.banking.finstream.finstream_backend.dto.request.PaymentRequest;
import com.banking.finstream.finstream_backend.dto.response.PaymentResponse;
import com.banking.finstream.finstream_backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatepayment(
            @RequestHeader(value = "X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request
    ){
        PaymentResponse response = paymentService.processPayment(idempotencyKey, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
