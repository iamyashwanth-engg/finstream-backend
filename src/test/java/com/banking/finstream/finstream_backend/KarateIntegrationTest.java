package com.banking.finstream.finstream_backend;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;

// This annotation starts your actual Spring Boot server on port 8080 for the test
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class KarateIntegrationTest {

    @Karate.Test
    Karate testPaymentsAPI() {
        // Tells Karate where to find the Gherkin scenarios
        return Karate.run("classpath:features/payment.feature");
    }
}