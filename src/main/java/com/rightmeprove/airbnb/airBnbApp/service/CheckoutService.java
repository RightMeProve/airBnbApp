package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.entity.Booking;

public interface CheckoutService {

    String getCheckoutService(Booking booking, String successUrl, String failureUrl);

}
