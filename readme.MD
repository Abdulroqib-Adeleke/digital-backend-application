# POI-BANK

POI-BANK is a Spring Boot backend for a digital banking / fintech platform. It supports customer and business account onboarding, KYC verification, tiered account limits, fund transfers and card deposits, PDF receipts and statements, email/OTP-based authentication, and an admin console for oversight and moderation.

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 21 |
| Framework | Spring Boot 4.1.0 (Web MVC, Data JPA, Validation, Security, Mail) |
| Database | PostgreSQL 16 |
| Auth | JWT (`jjwt`), stateless sessions, BCrypt password hashing |
| API Docs | springdoc-openapi (Swagger UI) |
| PDF Generation | iText 5, JasperReports fonts |
| Email | Spring Mail / Brevo API |
| Build Tool | Maven (wrapper included: `mvnw`) |
| Containerization | Docker, Docker Compose |
| Admin Tooling | pgAdmin (via Docker Compose) |

## Features

- **Authentication & Sessions** — JWT access/refresh tokens, OTP verification (email/SMS) on login, forgot-password flows for customers and admins, logout with session invalidation.
- **Account Management** — Personal (Savings, Checkings, Retirement, CD) and business account creation, secondary/multiple accounts per user, tiered account limits (`TIER_1`–`TIER_3`) with daily transfer limits and max-balance enforcement, transaction PIN setup/verification.
- **Transactions** — Peer transfers, transfers between a user's own accounts, card-based deposits, transaction requery (status reconciliation), full transaction history.
- **KYC** — Customer KYC submission (NIN/BVN), admin review queue, approve/reject workflows.
- **Admin Console** — Bank overview stats, customer/account suspension & reactivation, audit log viewing, paginated views of customers, transactions, and daily transaction summaries.
- **Documents & Notifications** — PDF transaction receipts and account statements (iText), statements delivered via email, transaction alert notifications.
- **Auditing** — Append-only `AuditLog` and `AccountDailyAudit`/ledger entries for traceability of balance-affecting actions.

## Architecture

The application follows a layered structure:

```
controller/   → REST endpoints (Auth, Account, Admin, Business, Transaction, KYC, Daily Transactions)
service/      → Business logic interfaces + impl/ implementations
repository/   → Spring Data JPA repositories
domain/
  entities/   → JPA entities (Account, Customer, Business, Transaction, LedgerEntry, KycEntity, ...)
  dto/
    request/  → Inbound request payloads
    response/ → Outbound response payloads
  enums/      → Domain enums (AccountStatus, AccountTier, TransactionStatus, Role, ...)
security/     → JWT filter, token service, Spring Security config, CORS config
notification/ → Email & OTP delivery, transaction alerts
pdf/          → Receipt and statement PDF generation (iText)
exceptions/   → Custom exceptions + global exception handler
utils/        → Encryption, tiering, account, transaction utility classes
```

### Security Model

- Stateless sessions (`SessionCreationPolicy.STATELESS`) with a custom `AuthFilter` validating JWTs on every request.
- Role-based authorization via `@PreAuthorize`, with three roles: `ADMIN`, `EMPLOYEE`, `CUSTOMER`.
- Public endpoints: `/api/auth/**`, `/api/account/create-personal-account`, `/api/business/createaccount`, and Swagger/OpenAPI docs. Everything else requires authentication.
- Sensitive fields (e.g. NIN/BVN, card details) are encrypted at rest via `EncryptionUtil` (Spring Security `TextEncryptor`).
- Passwords hashed with BCrypt.

### Account Tiers

| Tier | Max Transfer Limit | Max Balance |
|---|---|---|
| TIER_1 | 50,000 | 300,000 |
| TIER_2 | 200,000 | 500,000 |
| TIER_3 | 5,000,000 | 999,999,999 |

Daily transfer totals are tracked per account (`DailyTransferTotal`) and enforced by `TierLimiterUtil` before a transfer is allowed.

## API Overview

All endpoints are prefixed `/api`. Full interactive documentation is available via Swagger UI once the app is running (see [API Documentation](#api-documentation)).

### Auth — `/api/auth`
| Method | Endpoint | Description |
|---|---|---|
| POST | `/create-Admin` | Create an admin account |
| POST | `/create-personal-account` | Register a new customer (public) |
| POST | `/login` | Customer login (triggers OTP) |
| POST | `/verify-otp` | Verify OTP after login |
| POST | `/resend-otp` | Resend OTP |
| POST | `/login-admin` | Admin login |
| POST | `/logout` | Logout / invalidate session |
| GET | `/new-access-token/{refresh-token}` | Refresh access token |
| PATCH | `/forget-password/customer` | Customer password reset |
| PATCH | `/forget-password/admin/{admin-id}` | Admin password reset |

### Account — `/api/account` (role: `CUSTOMER`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/accountName/{account-number}` | Resolve account name from account number |
| PUT | `/set-transaction-pin` | Set transaction PIN |
| GET | `/verify-transaction-pin` | Verify transaction PIN |
| PATCH | `/change-transaction-pin` | Change transaction PIN |
| POST | `/create-secondary-account` | Open an additional account for the user |
| GET | `/user-profile` | Get current customer profile |
| PATCH | `/password-reset` | Change password (authenticated) |
| POST | `/get-statement-via-email` | Email an account statement |
| GET | `/download-pdf-receipt/{transaction-id}` | Download a transaction receipt as PDF |
| POST | `/download-statement` | Download an account statement as PDF |

### Business — `/api/business`
| Method | Endpoint | Description |
|---|---|---|
| POST | `/createaccount` | Register a new business account (public) |

### Transaction — `/api/transaction` (role: `CUSTOMER`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/transfer` | Transfer funds to another account |
| POST | `/transfer-within-user-accounts` | Transfer between the user's own accounts |
| POST | `/deposit` | Deposit funds via card |
| PUT | `/requery/{transaction-id}` | Requery/reconcile a transaction's status |
| GET | `/transaction-history/{account-number}` | Full transaction history for an account |

### KYC — `/api/kyc` (role: `CUSTOMER`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/submit` | Submit KYC documents (NIN/BVN) |

### Admin — `/api/admin` (role: `ADMIN`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/admin-profile` | Get current admin profile |
| GET | `/fetch-pending-kyc` | Paginated list of pending KYC submissions |
| GET | `/fetch-pending-kyc-by-account-number/{account-number}` | Pending KYC by account number |
| PATCH | `/approvekyc/{kyc-id}` | Approve a KYC submission |
| PATCH | `/reject-kyc` | Reject a KYC submission |
| PATCH | `/suspend-account` | Suspend a customer account |
| PATCH | `/reactivate-account/{account-number}` | Reactivate a suspended account |
| GET | `/customer-profile/{account-number}` | Get a customer's profile |
| GET | `/customers` | Paginated list of all customers |
| GET | `/transaction/{transaction-id}` | Get a transaction by ID |
| GET | `/customer-transactions/{account-number}` | All transactions for a customer |
| GET | `/stats` | Bank-wide overview statistics |
| GET | `/get-all-audit-logs` | Paginated audit log |
| GET | `/get-all-transactions` | Paginated list of all transactions |

### Daily Transactions — `/api/daily-transactions` (role: `ADMIN`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/daily` | Daily transaction summary |

## Getting Started

### Prerequisites

- Java 21 (JDK)
- Maven (or use the included `./mvnw` wrapper)
- PostgreSQL 16 (or Docker, see below)
- A Brevo (Sendinblue) account/API key for email delivery (optional but required for OTP/statement emails)

### Environment Variables

The application is configured entirely through environment variables (see `src/main/resources/application.properties`):

| Variable | Description |
|---|---|
| `db` | PostgreSQL JDBC URL, e.g. `jdbc:postgresql://localhost:5435/posi` |
| `username` | Database username |
| `password` | Database password |
| `JWT_SECRET` | Secret key used to sign JWTs |
| `JWT_EXPIRATION_TIME` | Access token expiry (ms) |
| `JWT_REFRESH_EXPIRATION_TIME` | Refresh token expiry (day) |
| `ENCRYPTION_PASSWORD` | Password for field-level encryption (`EncryptionUtil`) |
| `ENCRYPTION_SALT` | Salt for field-level encryption |
| `BREVO_API_KEY` | API key for the Brevo email service |
| `BREVO_SENDER_EMAIL` | Sender email address for outbound mail |

For local Docker Compose (Postgres + pgAdmin), also set:

| Variable | Description |
|---|---|
| `POSTGRES_USER` | Postgres superuser username |
| `POSTGRES_PASSWORD` | Postgres superuser password |
| `POSTGRES_DB` | Database name to create |
| `PGADMIN_EMAIL` | pgAdmin login email |
| `PGADMIN_PASSWORD` | pgAdmin login password |

Create a `.env` file in the project root (already gitignored) with the values above.

### Run the Database (Docker Compose)

```bash
docker compose up -d
```

This starts:
- **PostgreSQL 16** on host port `5435` (mapped to container `5432`)
- **pgAdmin 4** on `http://localhost:8081`

### Run the Application

**Locally with Maven wrapper:**

```bash
./mvnw spring-boot:run
```

Make sure the environment variables above are exported in your shell (or loaded via your IDE's run configuration) before starting.

**Build a JAR:**

```bash
./mvnw clean package
java -jar target/*.jar
```

**Fully Dockerized (build + run):**

```bash
docker build -t posi-fintech .
docker run -p 8080:8080 --env-file .env posi-fintech
```

The Dockerfile uses a two-stage build (`eclipse-temurin:21-jdk` for build, `eclipse-temurin:21-jre` for runtime) and exposes port `8080`.

The API will be available at `http://localhost:8080`.

### Database Schema / Migrations

SQL migration files live in `src/main/resources/db/migration/` (Flyway-style naming: `V1__init_schema.sql`, `V2__create_daily_transactions_schema.sql`, `V3__alter_users_and_businesses_scehma.sql`). 

## API Documentation

Once running, interactive API docs (Swagger UI) are available at:

```
http://localhost:8080/swagger-ui.html
```

Raw OpenAPI spec:

```
http://localhost:8080/v3/api-docs
```

## Testing

Run the test suite with:

```bash
./mvnw test
```

Test reports are written to `target/surefire-reports/`.

## Project Structure

```
posi-fintech/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/groupa/digitalbackendapplication/
    │   │   ├── controller/       # REST controllers
    │   │   ├── service/          # Service interfaces + impl/
    │   │   ├── repository/       # Spring Data JPA repositories
    │   │   ├── domain/
    │   │   │   ├── entities/     # JPA entities
    │   │   │   ├── dto/request/  # Request DTOs
    │   │   │   ├── dto/response/ # Response DTOs
    │   │   │   └── enums/        # Domain enums
    │   │   ├── security/         # JWT auth, security config, CORS
    │   │   ├── notification/     # Email & OTP delivery
    │   │   ├── pdf/               # PDF receipt/statement generation
    │   │   ├── exceptions/       # Custom exceptions + global handler
    │   │   ├── config/            # OpenAPI & Spring Data config
    │   │   └── utils/            # Encryption, tiering, misc utilities
    │   └── resources/
    │       ├── application.properties
    │       └── db/migration/     # SQL schema migration scripts
    └── test/                     # Unit/integration tests
```

## Domain Model Highlights

- **`User`** — base identity (auth credentials, role) subtyped by `Customer` / `Admin`.
- **`Account`** — belongs to a customer or business; tracks balance, `AccountStatus` (`PENDING_VERIFICATION`, `ACTIVE`, `DORMANT`, `FROZEN`), `AccountTier`, and account type.
- **`Business`** — business entity linked to business accounts.
- **`Transaction`** / **`LedgerEntry`** — records fund movement; ledger entries capture debit/credit legs with `LedgerEntryStatus` (`SETTLED`, `PENDING`, `VOID`) for double-entry-style auditability.
- **`KycEntity`** — KYC submissions with `KycStatus` (`PENDING`, `APPROVED`, `REJECTED`) and `KycDocumentType` (`NIN`, `BVN`).
- **`OtpVerification`** — one-time passcodes for login, sent via `OtpChannel` (`EMAIL`, `SMS`).
- **`LoginSession`** / **`RefreshSession`** — tracks active sessions and refresh tokens.
- **`AuditLog`** / **`AccountDailyAudit`** — records administrative and account-level actions (`ActionType` enum) for compliance and traceability.
- **`CardDetails`** — stored (encrypted) card details for deposit funding.
- **`DailyTransferTotal`** / **`DailyTransactions`** — per-account and bank-wide daily transaction aggregates, used for tier-limit enforcement and admin reporting.

## Notes & Caveats

- The `.env` file and IDE-specific settings (`.idea/`) are gitignored — do not commit secrets.

## License

No license file is currently included in this repository. Add a `LICENSE` file to clarify usage terms before distributing or open-sourcing this project.