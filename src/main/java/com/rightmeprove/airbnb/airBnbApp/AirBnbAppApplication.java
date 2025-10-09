package com.rightmeprove.airbnb.airBnbApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ⚡ Main Spring Boot Application class for AirBnB clone backend.
 *
 * Responsibilities:
 * 1. Bootstraps the Spring Boot application.
 * 2. Enables scheduling to allow @Scheduled tasks (e.g., price updates, inventory updates).
 */
@SpringBootApplication
@EnableScheduling // enables the processing of @Scheduled annotations
public class AirBnbAppApplication {

	public static void main(String[] args) {
		// Launch the Spring Boot application
		SpringApplication.run(AirBnbAppApplication.class, args);
	}
}
