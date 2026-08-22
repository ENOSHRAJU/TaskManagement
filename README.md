# Task Management System

A **production-inspired Task Management REST API** built using **Spring Boot** that demonstrates secure authentication, role-based authorization, refresh token authentication, email verification, password reset, and layered architecture following backend development best practices.

This project was developed to showcase production-ready backend concepts, clean architecture, secure authentication, and REST API design.

---

# Tech Stack

### Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL

### Authentication & Security

- JWT Authentication
- Refresh Tokens
- Email Verification Tokens
- Password Reset Tokens
- BCrypt Password Encryption

### Email Service

- Brevo SMTP

### Documentation

- Swagger / OpenAPI

### Build Tool

- Maven

---

# Features

## Authentication & Security

- User Registration
- Email Verification
- JWT Authentication
- Refresh Token Authentication
- Role-Based Authorization
- Password Reset via Email
- Secure Logout
- BCrypt Password Encryption
- Protected REST APIs
- Global Exception Handling
- Input Validation

---

## User Management

- Register new users
- Login with JWT Authentication
- Email verification before login
- Forgot Password
- Secure Password Reset
- Logout (Refresh Token Invalidation)

---

## Refresh Token Flow

- Secure Refresh Token Generation
- Refresh Token Validation
- Access Token Regeneration
- Refresh Token Expiry Validation

---

## Audit Logging

The application records important user activities including:

- User Registration
- Email Verification
- Password Reset
- Login
- Logout
- Account Activation

---

## API Documentation

Interactive API documentation using **Swagger UI**.

---

# Authentication Flow

```text
Register
      │
      ▼
Email Verification
      │
      ▼
Login
      │
      ▼
Access Token + Refresh Token
      │
      ▼
Protected APIs
      │
      ▼
Refresh Token Endpoint
      │
      ▼
New Access Token
```

---

# Security Features

- JWT-based Authentication
- Refresh Token Support
- Role-Based Access Control (RBAC)
- Email Verification
- Password Reset Tokens
- BCrypt Password Encryption
- Global Exception Handling
- Input Validation

---

# Database

The application uses **PostgreSQL** as the primary database.

### Main Entities

- User
- Role
- Project
- Task
- RefreshToken
- PasswordResetToken
- EmailVerificationToken
- AuditLog

---

# Project Structure

```text
src
├── Controllers
├── Service
├── Repository
├── Model
├── DTOs
├── Security
├── Exception
├── Configuration
├── Common
└── Enums
```

---

# Configuration

Sensitive values are configured using **environment variables**.

Example:

```properties
DB_URL=

DB_USERNAME=

DB_PASSWORD=

MAIL_USERNAME=

MAIL_PASSWORD=

JWT_SECRET=
```

---

# Running the Project

### Clone Repository

```bash
git clone https://github.com/ENOSHRAJU/TaskManagement.git
```

### Navigate

```bash
cd TaskManagement
```

### Configure Environment Variables

Set the following environment variables before running the application:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- MAIL_USERNAME
- MAIL_PASSWORD
- JWT_SECRET

### Run

```bash
mvn spring-boot:run
```

---

# Authentication APIs

| Method | Endpoint |
|---------|----------|
| POST | `/auth/register` |
| GET | `/auth/verify-email` |
| POST | `/auth/login` |
| POST | `/auth/refresh-token` |
| POST | `/auth/logout` |
| POST | `/auth/forgot-password` |
| POST | `/auth/reset-password` |

---

# Future Improvements
- Unit Testing using JUnit & Mockito
- Optimistic/Pessimistic Locking for Concurrent Task Updates
- Refresh Token Rotation for Enhanced Security
- Email Verification Token Expiry with Resend Verification Flow
- Docker Compose Support
- Redis Caching
- Spring Boot Actuator for Monitoring & Health Checks
- Rate Limiting for Authentication APIs
- Account Lockout after Multiple Failed Login Attempts

---

# Author

**Enosh Raju**

Java Backend Developer

GitHub: https://github.com/ENOSHRAJU
