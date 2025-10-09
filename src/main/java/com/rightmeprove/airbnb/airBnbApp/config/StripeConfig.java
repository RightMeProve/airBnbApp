package com.rightmeprove.airbnb.airBnbApp.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration // Marks this as a Spring configuration class (loaded at app startup)
public class StripeConfig {

    /**
     * Initializes Stripe with the secret API key from application properties.
     *
     * @param stripeSecretKey value injected from application.yml / properties file
     *
     * Why:
     * - Stripe requires a global API key before making any payment-related API calls.
     * - Setting it here ensures it's configured once at startup for the entire app.
     */
    public StripeConfig(@Value("${stripe.secret.key}") String stripeSecretKey) {
        Stripe.apiKey = stripeSecretKey; // Assigns API key to Stripe’s global configuration
    }
}
