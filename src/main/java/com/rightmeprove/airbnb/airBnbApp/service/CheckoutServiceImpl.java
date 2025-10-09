package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.entity.Booking;
import com.rightmeprove.airbnb.airBnbApp.entity.User;
import com.rightmeprove.airbnb.airBnbApp.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor // inject BookingRepository
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final BookingRepository bookingRepository;

    /**
     * Creates a Stripe Checkout Session for the given booking.
     * Steps:
     * - Retrieve the logged-in user
     * - Create a Stripe customer with user's name & email
     * - Create a Stripe checkout session with:
     *      - Payment mode
     *      - Billing address required
     *      - Customer attached
     *      - Success & failure URLs
     *      - Line item with hotel/room info and total amount
     * - Save the session ID in booking for future reference
     * - Return session URL for redirect
     */
    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {
        log.info("Creating session for booking with id: {}", booking.getId());

        // Get currently logged-in user
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            // 1️⃣ Create Stripe Customer object
            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .build();

            Customer customer = Customer.create(customerParams);

            // 2️⃣ Create checkout session
            SessionCreateParams sessionParams = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT) // one-time payment
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .setCustomer(customer.getId())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(failureUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount(
                                                            booking.getAmount()
                                                                    .multiply(BigDecimal.valueOf(100)) // convert to paise
                                                                    .longValue()
                                                    )
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(booking.getHotel().getName() + " : " + booking.getRoom().getType())
                                                                    .setDescription("Booking ID: " + booking.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            // 3️⃣ Create session on Stripe
            Session session = Session.create(sessionParams);

            // 4️⃣ Save session ID in booking for tracking
            booking.setPaymentSessionId(session.getId());
            bookingRepository.save(booking);

            log.info("Session created successfully for booking with ID: {}", booking.getId());

            // 5️⃣ Return checkout URL to frontend
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException(e); // bubble up Stripe errors
        }
    }
}
