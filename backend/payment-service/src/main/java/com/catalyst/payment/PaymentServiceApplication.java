package com.catalyst.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Payment Service.
 * This is a placeholder for Phase 3 implementation.
 */
@SpringBootApplication(scanBasePackages = {
    "com.catalyst.payment",
    "com.catalyst.shared"
})
public class PaymentServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}

