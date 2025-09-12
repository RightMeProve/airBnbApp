# 🏨 AirBnB (Spring Boot + Postgres)

A backend system that mimics the core functionality of Airbnb: managing
hotels, rooms, bookings, guests, inventory, and payments.

------------------------------------------------------------------------

## ⚡ Features (So far)

-   Hotel & Room management
-   Guest management
-   User authentication with roles (HotelManager, Guest)
-   Booking system with Guests + Payments
-   Inventory tracking with surge pricing
-   Payment integration (Transaction ID & Status)
-   Enum-based Role & Status management

------------------------------------------------------------------------

## 📐 System Design

### DFD (Data Flow Diagram)

![DFD](./DFD_airBnb.png)

### DFD (DBeaver View)

![DFD](./DFD_airBnb_DBeaverView.png)

### High-Level Architecture

![System Design](./Design_airBnb.png)

------------------------------------------------------------------------

## 🛠 Tech Stack

-   **Spring Boot**
-   **JPA/Hibernate**
-   **PostgreSQL**
-   **Lombok**
-   **Maven** 

------------------------------------------------------------------------

## ⚙️ Setup

1.  **Clone the repo**

    ``` bash
    git clone https://github.com/RightMeProve/airBnbApp.git
    cd airBnbApp
    ```

2.  **Configure Database**\
    In `src/main/resources/application.properties`:

    ``` properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/airbnb
    spring.datasource.username=postgres
    spring.datasource.password=yourpassword
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    ```

3.  **Run the application**

    ``` bash
    ./mvnw spring-boot:run
    ```

------------------------------------------------------------------------

## 📂 Project Roadmap

1.  ✅ Setup project & dependencies
2.  ✅ Configure DB connection
3.  ✅ Define entities & relationships
4.  ⬜ Implement repositories & services
5.  ⬜ Add REST controllers & APIs
6.  ⬜ Integrate authentication & JWT
7.  ⬜ Implement business rules (surge pricing, inventory reset job)
8.  ⬜ Deploy locally → then containerize with Docker

------------------------------------------------------------------------

## 👨‍💻 Author

-   **Satyam Kumar** ([@RightMeProve](https://github.com/rightMeProve))
