# ✈️ EaseGoFly Web

A comprehensive full-stack travel booking platform that enables users to search, compare, and book flights, hotels, buses, cabs, and holiday packages through a modern web interface. The project includes customer-facing applications, REST APIs, an administrative dashboard, payment integration, and booking management.

---

## 🚀 Features

### ✈ Flight Booking
- Flight search
- One-way, Round-trip & Multi-city
- Flight filters and sorting
- Fare details
- Traveller management
- Booking confirmation
- Ticket generation
- Search history

### 🏨 Hotel Booking
- Hotel search
- Room availability
- Hotel details
- Booking management

### 🚌 Bus Booking
- Bus search
- Seat selection
- Boarding & dropping points
- Online reservation

### 🚖 Cab Booking
- Cab search
- Driver assignment
- Trip management

### 🌍 Tour Packages
- Browse tour packages
- Package details
- Booking requests

### 👤 User Features
- Registration
- Login & Authentication
- Profile Management
- Wallet
- Booking History
- Coupons
- Reviews

### 💳 Payments
- Online payment integration
- Zaakpay Payment Gateway
- Transaction management
- Order invoices

### 📄 Ticket & Invoice
- PDF Ticket generation
- Booking Invoice
- Booking confirmation emails

### 📊 Admin Dashboard
- User Management
- Product Management
- Booking Management
- Coupon Management
- Wallet Management
- Reports
- Settings
- State & Country Management

---

# 🛠 Tech Stack

## Backend

- Java
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- REST API

## Frontend

- Thymeleaf
- HTML5
- CSS3
- Bootstrap
- JavaScript
- jQuery

## Database

- MySQL

## Build Tool

- Maven

## Authentication

- Spring Security

## Payment Gateway

- Zaakpay

## PDF

- iText PDF

---

# 📁 Project Structure

```text
EasyGoFlyProject
│
├── EasyGoFlyCommon
│
├── EasyGoFlyAPI
│   ├── Flight APIs
│   ├── Bus APIs
│   ├── Driver APIs
│   ├── Wallet APIs
│   ├── Payment APIs
│   └── Security
│
├── EasyGoFlyBackEnd
│   ├── Dashboard
│   ├── Product Management
│   ├── Customer Management
│   ├── Orders
│   ├── Coupons
│   └── Reports
│
└── Database
```

---

# 🔑 Key Modules

- Flight Booking
- Bus Booking
- Hotel Booking
- Cab Booking
- Tour Packages
- User Authentication
- Wallet System
- Coupon Engine
- Booking Management
- Payment Processing
- PDF Ticket Generation
- Admin Dashboard

---

# ⚙️ Installation

## Clone Repository

```bash
git clone https://github.com/rahulcivildp/Easegofly_web.git
```

## Enter Project

```bash
cd Easegofly_web
```

## Configure Database

Update:

```
application.yml
```

Example:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/easygofly
    username: root
    password: your_password
```

---

## Build

```bash
mvn clean install
```

---

## Run

```bash
mvn spring-boot:run
```

or run the main Spring Boot application from your IDE.

---

# 🔌 APIs

The project exposes REST APIs for:

- Flights
- Hotels
- Buses
- Wallet
- Drivers
- Products
- Payments

---

# Database

- MySQL
- Hibernate ORM
- Spring Data JPA

---

# Security

- Spring Security
- Authentication
- Authorization
- Session Management

---

# Future Improvements

- Flight fare alerts
- AI travel assistant
- Dynamic pricing engine
- Hotel recommendation system
- Travel analytics dashboard
- Mobile application
- Multi-language support

---

# License

This project is intended for educational and commercial learning purposes.

---

# Author

**Tanmay Sarkar**

Full Stack Developer

- Java
- Spring Boot
- REST APIs
- MySQL
- Flutter
- Cloud Technologies
