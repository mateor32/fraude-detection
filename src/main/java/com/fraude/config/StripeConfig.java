package com.fraude.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {


    @Value("${stripe.secret-key:}")
    private String secretKeyFromProperties;

    @PostConstruct
    public void init() {
        // Prioridad: variable de entorno > properties
        String envKey = System.getenv("STRIPE_SECRET_KEY");
        if (envKey != null && !envKey.isBlank()) {
            Stripe.apiKey = envKey;
        } else if (secretKeyFromProperties != null && !secretKeyFromProperties.isBlank()) {
            Stripe.apiKey = secretKeyFromProperties;
        } else {
            throw new IllegalStateException("Stripe Secret Key no configurada. Usa la variable de entorno STRIPE_SECRET_KEY.");
        }
    }
}
