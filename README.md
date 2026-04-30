# 🌾 AgriRent — Agriculture Equipment Rental System

> A Full Stack Web Application for renting agriculture equipment online, built with **Spring Boot + MySQL + HTML/CSS/JavaScript**.

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## 📌 Project Overview

**AgriRent** is a centralized agriculture equipment rental platform that connects farmers with available farming equipment. Farmers can browse, book, and pay for equipment rentals online, while administrators manage the entire system through a dedicated dashboard.

This project was developed as a **Final Year Project** demonstrating full-stack web development skills using modern technologies.

---

## 🚀 Features

### 👨‍🌾 Farmer (User) Features
- ✅ User Registration & Login with BCrypt password security
- ✅ Browse available equipment with search & filter
- ✅ View equipment details (category, price, location)
- ✅ Rent equipment by selecting date range
- ✅ UPI QR Code payment system
- ✅ Submit transaction ID for payment verification
- ✅ Download PDF Invoice for bookings
- ✅ View rental history (My Rentals)
- ✅ User Profile page with activity stats

### 👑 Admin Features
- ✅ Admin Dashboard with live statistics
- ✅ Equipment Management (Add, Edit, Delete)
- ✅ Booking Management (Confirm, Complete, Cancel)
- ✅ Payment Management (Verify, Confirm, Refund)
- ✅ User Management
- ✅ Revenue tracking
- ✅ Download PDF Invoice for any booking

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | HTML5, CSS3, JavaScript (Vanilla) |
| **Backend** | Java 17, Spring Boot 3.2.0 |
| **Database** | MySQL 8.0 |
| **ORM** | Spring Data JPA / Hibernate |
| **Security** | Spring Security + BCrypt |
| **PDF** | iText PDF Library |
| **Architecture** | MVC + REST API |

---

## 📁 Project Structure

```
Agrirent-fullstack/
│
├── backend/                          # Spring Boot Application
│   ├── src/main/java/com/agriculture/rental/
│   │   ├── config/                   # Security & Data Initializer
│   │   ├── controller/               # REST API Controllers
│   │   │   ├── AuthController.java
│   │   │   ├── EquipmentController.java
│   │   │   ├── BookingController.java
│   │   │   ├── PaymentController.java
│   │   │   ├── InvoiceController.java
│   │   │   └── AdminController.java
│   │   ├── dto/                      # Data Transfer Objects
│   │   ├── model/                    # Entity Classes (DB Tables)
│   │   │   ├── User.java
│   │   │   ├── Equipment.java
│   │   │   ├── Booking.java
│   │   │   └── Payment.java
│   │   ├── repository/               # Database Queries (JPA)
│   │   └── service/                  # Business Logic
│   └── src/main/resources/
│       └── application.properties    # DB Configuration
│
└── frontend/                         # HTML/CSS/JS Pages
    ├── index.html                    # Home Page
    ├── login.html                    # Login Page
    ├── register.html                 # Registration Page
    ├── equipment.html                # Browse Equipment
    ├── dashboard.html                # Admin Dashboard
    ├── payment.html                  # UPI Payment Page
    ├── profile.html                  # User Profile
    ├── css/                          # Stylesheets
    │   ├── style.css
    │   ├── home.css
    │   ├── auth.css
    │   ├── equipment.css
    │   └── dashboard.css
    └── js/                           # JavaScript Files
        ├── auth.js
        ├── main.js
        ├── equipment.js
        └── dashboard.js
```

---

## 🗄️ Database Tables

| Table | Description |
|-------|-------------|
| `users` | Registered farmers and admins |
| `equipment` | Farm equipment available for rent |
| `bookings` | Rental bookings made by farmers |
| `payments` | Payment records for bookings |

---

## 📡 REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/register` | Register new user |
| POST | `/api/login` | User login |
| GET | `/api/equipment` | Get all equipment |
| POST | `/api/equipment` | Add equipment (Admin) |
| PUT | `/api/equipment/{id}` | Update equipment (Admin) |
| DELETE | `/api/equipment/{id}` | Delete equipment (Admin) |
| POST | `/api/rent` | Create rental booking |
| GET | `/api/bookings` | Get all bookings |
| POST | `/api/payments` | Create payment |
| PUT | `/api/payments/{id}/process` | Confirm payment (Admin) |
| GET | `/api/invoice/{bookingId}` | Download PDF invoice |
| GET | `/api/admin/dashboard` | Dashboard statistics |

---

## ⚙️ Setup & Installation

### Prerequisites
- Java JDK 17+
- Maven 3.8+
- MySQL 8.0+

### Step 1 — Clone the repository
```bash
git clone https://github.com/dheepibalu/Agrirent-fullstack.git
cd Agrirent-fullstack/backend
```

### Step 2 — Configure Database
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/agriculture_rental?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 3 — Run the Application
```bash
mvn spring-boot:run
```

### Step 4 — Open in Browser
```
http://localhost:8080
```

---

## 🔑 Default Login Credentials

| Role | Username | Password |
|------|----------|----------|
| 👑 Admin | `admin` | `admin123` |
| 👨‍🌾 Farmer | `farmer1` | `farmer123` |

---

## 📸 Pages

| Page | URL |
|------|-----|
| Home | `http://localhost:8080` |
| Login | `http://localhost:8080/login.html` |
| Register | `http://localhost:8080/register.html` |
| Equipment | `http://localhost:8080/equipment.html` |
| Admin Dashboard | `http://localhost:8080/dashboard.html` |
| Payment | `http://localhost:8080/payment.html` |
| Profile | `http://localhost:8080/profile.html` |

---

## 👩‍💻 Developer

**Dheepibalu**
- GitHub: [@dheepibalu](https://github.com/dheepibalu)

---

## 📄 License

This project is licensed under the MIT License.

---

> 🌾 *AgriRent — Empowering farmers with the right tools at the right time.*
