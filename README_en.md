# Emergency Service

A project (not fully implemented) developed during an internship at **IFortex**.

## Project Idea

A SaaS platform that connects people in need of urgent medical assistance with the nearest qualified paramedics, ensuring a rapid response during the critical first minutes before public emergency services arrive.

## Current Implemented Features

*   **Comprehensive User Management:**
    *   **Registration:** Separate flows for clients and paramedics with mandatory email verification via OTP (codes are stored in Redis).
    *   **Authentication:** Support for login via email/password (with JWT) and through social networks (Google OAuth2).
    *   **Two-Factor Authentication (2FA):** Enabled by default to enhance account security.

*   **Flexible Authorization System:**
    *   Implemented a model based on **permissions**, rather than hard-coded roles.
    *   **Centralized access control** at the API Gateway level, preventing unauthorized requests before they reach internal services.

*   **Multi-step KYC Verification for Paramedics:**
    *   **Secure Document Upload:** Uploading and storing documents (passport, certificate) in **Azure**.
    *   **Admin Panel:** An interface for administrators to review and verify applications.
    *   **Automation:** Automatic creation of a paramedic account after application approval and sending a temporary password.

*   **Subscription Management via Stripe:**
    *   **Full Payment Cycle:** Creation of sessions for purchasing a subscription (Checkout Session) and managing it (Customer Portal).
    *   **Synchronization:** Using **webhooks** to automatically update the subscription status in the local database.

*   **Asynchronous Notification System:**
    *   **Service Decoupling:** Sending email notifications (OTP, application statuses, system alerts) via **RabbitMQ** and a separate **Notification Service**.
    *   **Integration with SendGrid:** Using a third-party provider for email delivery.

*   **Administrative Functions:**
    *   **User CRUD Operations:** A full set of tools for administrators to manage users.
    *   **Account Blocking:** The ability to temporarily (with a specified duration) or permanently block users. When blocked, all active user sessions (refresh tokens) are invalidated.
    *   **Password Reset:** Two mechanisms for an administrator to reset a user's password: generating a temporary password or sending a reset link.

*   **Centralized Routing and Security:**
    *   **Single Entry Point:** All requests to the system go through the **API Gateway (Spring Cloud Gateway)**.
    *   **Gateway-level Security:** The Gateway validates JWTs and enriches requests to internal services by adding the `X-User-Id` header.
---

## 🏗️ System Architecture

The system consists of several independent microservices that interact with each other synchronously and asynchronously.

*   **API Gateway (`api-gateway`)**: The single entry point. Responsible for request routing, centralized JWT authentication, and adding the `X-User-Id` header for internal services.
*   **User Service (`user-service`)**: The core of the system. Stores user data, manages authentication, authorization, and the KYC process.
*   **Billing Service (`billing-service`)**: Handles all logic related to payments and subscriptions via Stripe.
*   **Notification Service (`notification-service`)**: Receives messages from the RabbitMQ queue and sends email notifications via SendGrid.

---

## 🛠️ Tech Stack

| Category                   | Technologies                                                                                                                    |
|-----------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| **Backend**                 | Java 17, Spring Boot 3, Spring Cloud (Gateway, OpenFeign), Spring Security, Spring Data JPA, Liquibase                        |
| **Databases & Cache**       | PostgreSQL, Redis                                                                                                             |
| **Message Queues**          | RabbitMQ                                                                                                                      |
| **Integrations**            | Stripe API (for payments), <br/>SendGrid (for sending emails), <br/>Azure (for photo storage), <br/>Google OAuth2                |
| **Testing**                 | JUnit 5, Mockito, WireMock                                                                                                    |
| **Build Tools & Others**    | Docker, Docker Compose, Maven, Git, Postman                                                                                   |
| **Principles & Patterns**   | SOLID, REST, Microservice Architecture, MVC                                                                                   |

---

## 💡 Interesting Implementation Details

### 1. Flexible Permission-Based Authorization

Instead of binding endpoints to specific roles (`@PreAuthorize("hasRole('ADMIN')")`), I implemented a model where each role has a set of permissions (`admin:user:read`, `subscription:create`).

*   **How it works:** When a JWT is generated, an `authorities` claim is added to it, containing a list of all the user's permissions. The API Gateway and backend services check for the required permission (`@PreAuthorize("hasAuthority('...')")`).
*   **Why this approach:** It allows for creating new roles and changing access rights on the fly through the database, without rewriting or rebuilding the application.

### 2. Full KYC Verification Cycle with Azure (using Azurite locally)

1.  A user uploads documents via the API.
2.  The `User Service` validates the files and uploads them to **Azure**, storing only the secure file paths in its own database.
3.  After email verification, the application appears in the administrator's dashboard.
4.  Upon approval, the system automatically creates a new account with the `ROLE_PARAMEDIC` role and a temporary password, sending all necessary notifications.

### 3. Integration with Stripe via Webhooks

* **Flow:** A user pays for a subscription -> Stripe sends a webhook to our endpoint -> The service verifies the cryptographic signature of the request -> The subscription status is updated in the local database.

---