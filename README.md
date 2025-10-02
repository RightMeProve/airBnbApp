# 🏨 AirBnB Backend (Spring Boot + PostgreSQL)

A backend system that mimics the core functionality of Airbnb: managing hotels, rooms, bookings, guests, inventory, and payments.

---

## ⚡ Features (Implemented)

- **Hotel & Room management**
- **Guest management**
- **User authentication with roles** (`HotelManager`, `Guest`)
- **Booking system** with guests and payments
- **Inventory tracking** with surge pricing
- **Payment integration** (Transaction ID & Status)
- **Enum-based Role & Status management**
- **Dynamic Pricing Engine (Strategy Pattern):**
    - **Base Pricing**
    - **Surge Pricing**
    - **Occupancy-based Pricing**
    - **Urgency-based Pricing**
    - **Holiday Pricing**
- **Scheduled Price Updates:**
    - **Re-calculates inventory prices every hour**
    - **Updates minimum hotel price (per day) for faster search**

---

## 📐 System Design

### Data Flow Diagram (DFD)
![DFD](./DFD_airBnb.png)

### Database View (DBeaver)
![DFD DBeaver](./DFD_airBnb_DBeaverView.png)

### High-Level Architecture
![System Design](./Design_airBnb.png)

---

## 🛠 Tech Stack

- **Backend:** Spring Boot
- **ORM:** JPA / Hibernate
- **Database:** PostgreSQL
- **Utilities:** Lombok
- **Build Tool:** Maven

---

## 🗂 Controllers & Endpoints

### Hotel Management (Admin)
`/admin/hotels`
- `POST /` → Create new hotel
- `GET /` → Get all hotels
- `GET /{hotelId}` → Get hotel by ID
- `PUT /{hotelId}` → Update hotel
- `DELETE /{hotelId}` → Delete hotel
- `PATCH /{hotelId}/activate` → Activate hotel

### Room Management (Admin)
`/admin/hotels/{hotelId}/rooms`
- `POST /` → Create new room
- `GET /` → Get all rooms in a hotel
- `GET /{roomId}` → Get room by ID
- `DELETE /{roomId}` → Delete room

### Hotel Browsing
`/hotels`
- `GET /search` → Search hotels with filters
- `GET /{hotelId}/info` → Get detailed hotel info

### Booking
`/bookings`
- `POST /init` → Initialise a new booking
- `POST /{bookingId}/addGuests` → Add guests to a booking

---

## ⚙️ Setup Guide

1. **Clone the repository**
   ```bash
   git clone https://github.com/RightMeProve/airBnbApp.git
   cd airBnbApp
   ```

2. **Configure Database**  
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/airbnb
   spring.datasource.username=postgres
   spring.datasource.password=yourpassword
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

---

## 📂 Project Roadmap

1. ✅ Project setup & dependencies
2. ✅ Configure database connection
3. ✅ Define entities & relationships
4. ✅ Implement repositories & services
5. ✅ Add REST controllers & APIs
6. ✅ Implement dynamic pricing strategies (Strategy Pattern)
7. ✅ Add scheduled pricing updates (hourly)
8. ✅ Integrate authentication & JWT
9. ✅ Implement business rules (inventory reset job)
10. ⬜ Deploy locally → containerize with Docker

---

# 🚀 Airbnb Backend – Development Workflow

This section tracks the step-by-step workflow for building and maintaining the Airbnb backend project.  
It acts as both a **guide for new contributors** and a **progress log**.

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
- [x] Continuously test APIs with Postman (watch for DB constraint errors like unique, nullable, duplicates)

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
      ⚠️ Note: Cannot use `@NoArgsConstructor` with `private final` fields like `PricingStrategy` (since final fields must be initialized).

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

## 📓 Notes / Learnings
- Spring Security exceptions live in a different context than MVC exceptions, so they must be explicitly passed to `RestControllerAdvice`.
- Exception imports must use Spring Security packages (not `javax` or unrelated).
- Decorator Pattern is ideal for extending pricing logic dynamically.

---

## 👨‍💻 Author

- **Satyam Kumar** [@RightMeProve](https://github.com/RightMeProve)
