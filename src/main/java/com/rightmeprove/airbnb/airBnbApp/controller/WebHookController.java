package com.rightmeprove.airbnb.airBnbApp.controller;

import com.rightmeprove.airbnb.airBnbApp.service.BookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook") // Base route for webhook endpoints
@RequiredArgsConstructor // Injects BookingService automatically
public class WebHookController {

    private final BookingService bookingService;

    @Value("${stripe.webhook.secret}") // Secret to verify Stripe events
    private String endpointSecret;

    /**
     * Stripe webhook endpoint to capture payment events.
     * Verifies the signature to ensure events are from Stripe.
     *
     * @param payload raw JSON payload from Stripe
     * @param sigHeader Stripe-Signature header for verification
     * @return 204 No Content if payment processed successfully
     */
    @PostMapping("/payment")
    public ResponseEntity<Void> capturePayments(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            // Construct and verify the Stripe event
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            // Delegate processing to BookingService (e.g., update booking status)
            bookingService.capturePayment(event);

            return ResponseEntity.noContent().build(); // success
        } catch (SignatureVerificationException e) {
            // If signature verification fails, reject the request
            throw new RuntimeException(e);
        }
    }
}
