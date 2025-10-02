# Airbnb Backend – Development Workflow

This document tracks the step-by-step workflow for building and maintaining the Airbnb backend project.  
It acts as both a **guide for new contributors** and a **progress log** for ongoing development.

---

## 1. Project Setup
- [x] Initialize Spring Boot project with required dependencies (Spring Web, JPA, Validation, Lombok, etc.)
- [x] Configure DB connection in `application.properties`
- [x] Run the application and verify successful DB connection

---

## 2. Project Structure & Entities
- [x] Define project package structure (controller, service, repository, dto, exception, security, config)
- [x] Create Entities (Hotel, Room, User, Booking, Inventory, etc.)
- [x] Map relationships (OneToMany, ManyToOne, etc.)
- [x] Create Repositories (JpaRepository interfaces)
- [x] Create DTOs for request/response

---

## 3. Business Logic Layer
- [x] Implement Services for core business logic
- [x] Write Controllers (`HotelController`, `RoomController`, etc.)
- [x] Implement Exception Handling
  - [x] `ResourceNotFoundException` (extends RuntimeException)
  - [x] `ApiError`, `ApiResponse`
  - [x] `GlobalExceptionHandler`, `GlobalResponseHandler`

---

## 4. Inventory & API Testing
- [x] Implement `Inventory` logic
- [x] Continuously test APIs with Postman (watch out for DB constraint errors like unique, nullable, duplicates, etc.)

---

## 5. Search & Booking
- [x] Create `HotelBrowseController`
- [x] Implement Search functionality
- [x] Implement Booking functionality

---

## 6. Dynamic Pricing
- [x] Use **Decorator Design Pattern** for dynamic pricing strategies:
  - [x] Base Pricing
  - [x] Surge Pricing
  - [x] Occupancy-based Pricing
  - [x] Urgency-based Pricing
  - [x] Holiday Pricing
- ⚠️ Note: Cannot use `@NoArgsConstructor` with `private final` fields like `PricingStrategy` (since final fields must be initialized).

---

## 7. Security Implementation
- [x] Add Spring Security + JWT dependencies to `pom.xml`
- [x] Update `User` entity to implement `UserDetails`
  - [x] Implement `getAuthorities()`
  - [x] Implement `getUsername()`
- [x] Create `security` package for security-related code
- [x] Implement `JWTService` (generic, reusable across projects)
- [x] Add `JWT_SECRET` key in `application.properties`
- [x] Implement `JWTAuthFilter` (generic, reusable)
- [x] Create `WebSecurityConfig`
  - [x] Define public vs authenticated routes

---

## 8. Authentication Flow
- [x] Implement `AuthService` with methods: `login`, `signup`, `refreshToken`
- [x] Add DTO validation (`SignUpDto`, `LoginDto`)
- [x] Implement `AuthController` with exception handling
- [x] Manage `JwtException` & `AccessDeniedException`
  - [x] Use `HandlerExceptionResolver` in `JWTAuthFilter` to forward security exceptions to `RestControllerAdvice`

---

## 9. Token Management
- [x] Implement `refreshToken` generation in `AuthService`
- [x] Add refresh route in `AuthController`

---

## Notes / Learnings
- Spring Security exceptions live in a different context than MVC exceptions, so they must be explicitly passed to `RestControllerAdvice`.
- Exception imports must use Spring Security packages (not `javax` or unrelated).
- Decorator Pattern is ideal for extending pricing logic dynamically.  
