# Currency Exchange Platform (Mock Trading) 🚀

A microservice-based mock currency and gold trading platform designed to simulate real-world exchange operations with a focus on security, scalability, and modern software engineering principles.

## Table of Contents

1.  [Project Purpose](#project-purpose)
2.  [Features](#features)
3.  [Technical Stack](#technical-stack)
4.  [High-Level Architecture](#high-level-architecture)
5.  [Directory Structure](#directory-structure)
6.  [Core Concepts](#core-concepts)
    *   [Microservices Overview](#microservices-overview)
    *   [Security Flow & Header Decoding](#security-flow--header-decoding)
    *   [Asynchronous Processing with RabbitMQ](#asynchronous-processing-with-rabbitmq)
7.  [Prerequisites](#prerequisites)
8.  [Installation and Setup](#installation-and-setup)
    *   [Environment Variables](#environment-variables)
    *   [Building the Backend](#building-the-backend)
    *   [Running with Docker Compose](#running-with-docker-compose)
9.  [Running Individual Services (Optional - For Development)](#running-individual-services-optional---for-development)
10. [Key API Endpoints (via API Gateway)](#key-api-endpoints-via-api-gateway)
    *   [Authentication Endpoints (`/api/auth`)](#authentication-endpoints-apiauth)
    *   [Account Service Endpoints (`/api/account`)](#account-service-endpoints-apiaccount)
    *   [Exchange Service Endpoints (`/api/exchange`)](#exchange-service-endpoints-apiexchange)
11. [Accessing the Applications](#accessing-the-applications)
12. [Testing Strategy](#testing-strategy)
13. [Potential Future Enhancements](#potential-future-enhancements)
14. [Contributing](#contributing)
15. [License](#license)
16. [Acknowledgments](#acknowledgments)
17. [AI Tools Used](#ai-tools-used)


## Project Purpose

The primary objectives of this project are:

*   To design, implement, and orchestrate a distributed system based on a **microservice architecture**.
*   To gain hands-on experience with a modern Java & Spring ecosystem (Java 17, Spring Boot 3.x, Spring Cloud) for backend development and a contemporary JavaScript framework (Next.js/React) for the frontend.
*   To build a robust and secure **authentication and authorization mechanism** using JWT (JSON Web Tokens) and best-practice cookie management (HttpOnly, Secure).
*   To explore and implement **asynchronous communication patterns** using RabbitMQ to enhance system responsiveness, resilience, and decoupling between services.
*   To master **containerization** with Docker and multi-container application orchestration with Docker Compose for consistent development, testing, and deployment environments.
*   To apply **SOLID principles and Clean Code** practices in the context of a complex, real-world-like application.
*   To simulate FX and gold trading operations, integrating with external APIs (mocked for rate fetching) to provide a dynamic user experience.

## Features

✅ User Registration and JWT-based Authentication
✅ Secure Session Management using HttpOnly and Secure cookies for JWTs
✅ Centralized API Gateway for routing, security, and request/response manipulation
✅ Dynamic Service Discovery and Registration with Netflix Eureka
✅ Multi-Currency Wallet Management (e.g., TRY, USD, EUR, XAU/GOLD)
✅ Simulated (Mock) Deposit and Withdrawal Functionality
✅ Real-time (Mock) FX and Gold Price Fetching from External APIs (API Layer, ExchangeRate-API)
✅ Mock Currency and Gold Trading/Conversion operations
✅ Asynchronous processing of critical operations (e.g., balance checks, wallet updates) via RabbitMQ
✅ Independent PostgreSQL databases for each core microservice, ensuring data isolation
✅ Reverse Proxy and basic Load Balancing capabilities provided by Nginx
✅ Modern, interactive, and responsive Single-Page Application (SPA) frontend built with Next.js (React)
✅ Global state management in the frontend using Redux Toolkit
✅ Comprehensive Unit and Integration Test coverage for backend services

## Technical Stack

*   **Backend & Core:**
    *   Java 17
    *   Spring Boot 3.4.5
    *   Spring Cloud (Spring Cloud Gateway, Netflix Eureka Client)
    *   Spring Security (for JWT handling and authorization)
    *   Spring Data JPA (with Hibernate)
    *   Lombok
*   **Databases:** PostgreSQL 14 (Separate instances for Account and Exchange services)
*   **Messaging Queue:** RabbitMQ
*   **API Gateway:** Spring Cloud Gateway
*   **Service Discovery:** Netflix Eureka (using `steeltoeoss/eureka-server` Docker image)
*   **Web Server/Reverse Proxy:** Nginx
*   **Frontend:**
    *   Next.js 15
    *   React 19
    *   Redux Toolkit
    *   Axios
    *   Tailwind CSS (veya kullandığınız diğer CSS çözümü)
*   **Containerization & Orchestration:** Docker, Docker Compose
*   **Authentication/Authorization:** JSON Web Tokens (JWT)
*   **External API Integrations (Mock Data):** API Layer, ExchangeRate-API
*   **Build Tool:** Apache Maven (with a Parent POM structure for backend modules)
*   **Testing:**
    *   JUnit 5
    *   Mockito
    *   Spring Boot Test
    *   Testcontainers (for PostgreSQL and RabbitMQ integration tests)
*   **Development Principles:** SOLID, Clean Code, OOP, RESTful API Design

## High-Level Architecture

Markdown
+-------------------+      +-------+      +-----------------+      +--------------------+      +---------------------+
| Client (Browser)  |----->| Nginx |----->| API Gateway     |----->| Account Service    |----->| Account PostgreSQL  |
+-------------------+      +-------+      |(Spring Cloud GW)|      |(Spring Boot, Java)|      +---------------------+
                             |             +-----------------+      +--------------------+                ^
                             |                     |                           |                          |
                             |                     |                           | (via RabbitMQ)           | (via RabbitMQ)
                             |                     |                           v                          v
                             |                     |                    +-----------+             +---------------------+
                             |                     +------------------->| Exchange Service   |----->| Exchange PostgreSQL |
                             |                                          |(Spring Boot, Java)|      +---------------------+
                             |                                          +--------------------+                |
                             |                                                      | (HTTP to External)      |
                             |                                                      v                         |
                             |                                          +----------------------+            |
                             |                                          | External Rate APIs   |            |
                             |                                          |(API Layer, etc.)     |<------------
                             |                                          +----------------------+
                             |
                             |      +-----------------+      +-----------------+
                             +----->| Eureka Server   |<-----| All Services    |
                                    +-----------------+      +-----------------+

## Directory Structure

currency-exchange-platform/
├── backend/
│ ├── pom.xml (Parent POM for backend modules)
│ ├── accountService/
│ │ ├── src/
│ │ ├── Dockerfile
│ │ └── pom.xml
│ ├── exchangeService/
│ │ ├── src/
│ │ ├── Dockerfile
│ │ └── pom.xml
│ └── apiGateway/
│ ├── src/
│ ├── Dockerfile
│ └── pom.xml
├── docker/
│ ├── docker-compose.yml
│ ├── .env (ve .env.example)
│ └── nginx/
│ └── default.conf
├── frontend/ (fttech-exchange-app-frontend)
│ ├── ... (Next.js proje dosyaları)
│ └── Dockerfile
└── README.md
## Core Concepts

### Microservices Overview

*   **AccountService:** Manages all aspects of user accounts, including registration, secure login (JWT generation), user profile information, multi-currency wallet balances, and handling (mock) deposit/withdrawal transactions. It interacts directly with its dedicated PostgreSQL database.
*   **ExchangeService:** Responsible for fetching (mock) real-time exchange rates from configured external APIs. It processes currency conversion and mock trading requests, interacting with `AccountService` asynchronously via RabbitMQ for critical balance checks and final wallet updates. It also has its own PostgreSQL database for storing trade-related data.
*   **APIGateway (Spring Cloud Gateway):** Serves as the single entry point for all client requests. It handles dynamic routing to appropriate backend microservices (discovered via Eureka), performs centralized security checks (JWT validation), and manipulates request headers (e.g., adding the `X-User` header after URL-encoding the username extracted from the JWT).
*   **Eureka Server:** Provides service discovery capabilities, allowing microservices to register themselves and enabling the API Gateway and other services to dynamically locate instances of required services.
*   **RabbitMQ:** Facilitates asynchronous communication between microservices, particularly for operations that can be decoupled to improve user experience and system resilience, such as post-trade wallet updates or pre-trade balance verifications.

### Security Flow & Header Decoding

1.  User registers or logs in through endpoints routed via the API Gateway to `AccountService`.
2.  `AccountService` validates credentials and, upon successful authentication, generates a JWT. This JWT is then set as an `HttpOnly` and `Secure` (in production) cookie in the HTTP response.
3.  For subsequent requests to protected API endpoints:
    *   The browser automatically includes the JWT cookie with the request.
    *   The API Gateway intercepts the request. Its `JwtTokenValidator` component validates the JWT.
    *   If valid, the `JwtCookieToHeaderFilter` (in API Gateway) extracts the username (subject) from the JWT, **URL-encodes** it, and adds it as an `X-User` header to the request.
    *   The request, now with the `X-User` header, is forwarded to the appropriate backend microservice.
    *   In the backend service, a custom `DecodingUserHeaderFilter` (a `jakarta.servlet.Filter`) intercepts the incoming request.
    *   This filter specifically looks for the `X-User` header, **URL-decodes** its value (e.g., `%C5%9Fefik` becomes `şefik`), and wraps the original `HttpServletRequest` to provide the decoded username.
    *   The controller's `@RequestHeader("X-User") String username` parameter then receives the correctly decoded username.

### Asynchronous Processing with RabbitMQ

*   **Trade Execution:** When a user initiates an exchange in `ExchangeService`, it publishes messages to RabbitMQ queues for tasks like balance verification and wallet updates.
*   **Decoupled Consumers:** `AccountService` listens to these queues and processes these tasks asynchronously.
*   **Benefits:** Improves `ExchangeService` responsiveness, enhances system resilience, and reduces direct inter-service dependencies.

## Prerequisites

*   Java Development Kit (JDK) 17 or later
*   Apache Maven 3.6.x or later
*   Docker Engine and Docker Compose
*   Node.js (v18+) and npm/yarn (for frontend development if running locally)
*   An IDE (e.g., IntelliJ IDEA, VS Code)

## Installation and Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/engineerdavut/currency-exchange-platform.git
    cd currency-exchange-platform
    ```

2.  **Environment Variables:**
    *   Navigate to the `docker/` directory.
    *   Create a `.env` file (you can copy `.env.example` if it exists or create a new one).
    *   Populate it with your configurations (see `docker-compose.yml` for required variables):
        ```dotenv
        # Example .env content:
        ACCOUNT_POSTGRES_USER=your_user
        ACCOUNT_POSTGRES_PASSWORD=your_password
        ACCOUNT_POSTGRES_DB=account_db
        EXCHANGE_POSTGRES_USER=your_user
        EXCHANGE_POSTGRES_PASSWORD=your_password
        EXCHANGE_POSTGRES_DB=exchange_db
        RABBITMQ_HOST=rabbitmq
        RABBITMQ_PORT=5672
        RABBITMQ_USER=guest
        RABBITMQ_PASSWORD=guest
        EUREKA_URI=http://eureka-server:8761/eureka
        JWT_SECRET=YourSuperStrongAndLongSecretKeyForJWT-AtLeast32Bytes
        JWT_EXPIRATION=3600000
        EXCHANGE_API_LAYER_KEY=YourApiLayerKey
        EXCHANGE_EXCHANGERATE_KEY=YourExchangeRateApiKey
        # Ensure NEXT_PUBLIC_API_URL points to your Nginx (e.g., http://localhost/api for local Docker)
        NEXT_PUBLIC_API_URL=http://localhost/api 
        CORS_ALLOWED_ORIGINS=http://localhost # Or your frontend's actual origin in production
        CORS_ALLOWED_ORIGINS_FROM_ENV_FOR_JAVA_CONFIG=${CORS_ALLOWED_ORIGINS}
        ```
    *   **Important:** Replace placeholder values with your actual credentials and keys. `JWT_SECRET` must be strong.

3.  **Building the Backend Microservices:**
    Navigate to the `backend/` directory (where your parent `pom.xml` is located):
    ```bash
    mvn clean install -U
    ```
    To skip tests:
    ```bash
    mvn clean install -U -DskipTests
    ```

4.  **Running with Docker Compose:**
    From the `docker/` directory (where your `docker-compose.yml` is located):
    ```bash
    docker-compose up --build
    ```
    To run in detached mode:
    ```bash
    docker-compose up --build -d
    ```
    To stop services:
    ```bash
    docker-compose down
    ```
    To view logs:
    ```bash
    docker-compose logs -f <service_name> # e.g., account-service
    ```

## Running Individual Services (Optional - For Development)

1.  Start essential services via Docker Compose: `docker-compose up -d account-db exchange-db rabbitmq eureka-server nginx`.
2.  Configure your IDE to provide the necessary environment variables for the service you want to run locally.
3.  Run the Spring Boot application (e.g., `AccountServiceApplication.java`) from your IDE or using `mvn spring-boot:run` in its module directory.
4.  Run the frontend: `cd frontend && npm run dev`.

## Key API Endpoints (via API Gateway)

All endpoints are prefixed with `/api` and routed through Nginx and then API Gateway.
(e.g., `http://localhost/api/auth/login` or `https://exchangeplatform.hacigodavutaktas.online/api/auth/login`)

### Authentication Endpoints (`/api/auth`)
*   `POST /register`: Body: `{ "username": "user", "password": "password" }`
*   `POST /login`: Body: `{ "username": "user", "password": "password" }`
*   `POST /logout`
*   `GET /check`

### Account Service Endpoints (`/api/account`)
(Requires `X-User` header, provided by API Gateway)
*   `GET /info`
*   `GET /wallet`
*   `GET /transactions?currencyType=TRY`
*   `POST /deposit`: Body: `{ "currencyType": "TRY", "amount": 100.00 }`
*   `POST /withdraw`: Body: `{ "currencyType": "USD", "amount": 50.00 }`

### Exchange Service Endpoints (`/api/exchange`)
(Requires `X-User` header, provided by API Gateway)
*   `GET /rates`
*   `GET /rates/{baseCurrency}`
*   `POST /process`: Body: `{ "fromCurrency": "USD", "toCurrency": "TRY", "fromAmount": 10.00 }`

## Accessing the Applications

*   **Frontend UI:** `http://localhost` (via Nginx) or `https://exchangeplatform.hacigodavutaktas.online`
*   **API Gateway (Direct):** `http://localhost:8090`
*   **Eureka Dashboard:** `http://localhost:8761`
*   **RabbitMQ Management:** `http://localhost:15672` (default: guest/guest)

## Testing Strategy

*   **Unit Tests:** JUnit 5 & Mockito for services, utilities in each backend module.
*   **Repository Tests (`@DataJpaTest`):** Focused tests for Spring Data JPA repositories, often using H2 or Testcontainers.
*   **Integration Tests (`@SpringBootTest`):** Test component interactions within services, using Testcontainers for PostgreSQL & RabbitMQ.
*   **API Gateway Tests:** Validate routing and filter logic.

Run backend tests (from `backend/` directory):
```bash
mvn test
```

## Potential Future Enhancements

*   WebSocket integration for real-time price updates to the frontend.
*   Implementation of advanced trading order types (e.g., limit orders, stop-loss orders).
*   Development of user portfolio tracking features with more detailed analytics and historical performance.
*   Enhancement of security with Two-Factor Authentication (2FA).
*   Integration of a caching layer (e.g., using Redis) for frequently accessed data to improve response times and reduce database load.
*   Setting up comprehensive system monitoring and visualization using Prometheus and Grafana.
*   Establishing a CI/CD pipeline (e.g., with GitHub Actions or Jenkins) for automated builds, testing, and deployments.
*   Further customization and detailing of API documentation using OpenAPI/Swagger (via `springdoc-openapi`).

## Contributing

Contributions, issues, and feature requests are welcome! Please feel free to check the [issues page](https://github.com/engineerdavut/currency-exchange-platform/issues) on the GitHub repository.

If you'd like to contribute:

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE.txt` in the repository for more information.


## Acknowledgments

*   The vibrant communities behind Spring Boot, Spring Cloud, and the Java ecosystem.
*   The developers and maintainers of Next.js, React, and the broader JavaScript frontend ecosystem.
*   Providers of the external APIs (API Layer, ExchangeRate-API) used for sourcing mock data.
*   The creators of essential tools like Docker, Nginx, PostgreSQL, and RabbitMQ.
*   *(Any other specific libraries, tools, or individuals you wish to thank)*

## AI Tools Used

This project, including aspects of its design, development, debugging, documentation, and testing, benefited from the assistance and capabilities of several AI-powered tools. My usage of these tools aimed to enhance productivity, explore different approaches, and generate boilerplate or alternative solutions. The AI tools utilized include:

*   Windsurf
*   Perplexity AI
*   ChatGPT (by OpenAI)
*   Gemini (by Google)