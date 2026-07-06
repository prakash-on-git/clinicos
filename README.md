# ClinicOS — Multi-Tenant Clinic Management Platform

A production-grade REST API built with **Spring Boot 4.1 / Java 21 / PostgreSQL** covering the full lifecycle of a modern clinic — appointments, queue management, billing, electronic medical records, notifications, audit logs, patient portal, and SaaS subscription billing.

> **Live API docs →** `http://localhost:8080/swagger-ui/index.html` after running locally

---

## What makes this project different

| Feature | What it demonstrates |
|---------|---------------------|
| **Multi-constraint availability algorithm** | Resolves doctor schedule + breaks + day overrides + leaves + clinic hours + closures + existing bookings simultaneously to compute free 10-min slots |
| **Race-condition-safe double booking** | Dual-layer prevention: application-level slot validation + DB-level interval overlap query as a safety net |
| **Compliance-grade audit trail** | Immutable before/after JSON snapshots on every clinical data mutation with actor attribution and IP resolution |
| **Multi-tenant SaaS subscription enforcement** | Per-clinic plan limits (doctor cap, monthly patient cap) enforced at creation time with HTTP 402 |
| **Walk-in queue management** | Per-doctor daily token sequencing, status lifecycle, average wait/consultation time tracking |
| **Appointment rescheduling via linked records** | Creates a new appointment and marks the old as RESCHEDULED preserving full history chain via `rescheduledFromId` |
| **RFC 7807 Problem Detail errors** | Standardised error responses with correct HTTP semantics (402, 409, 404, etc.) throughout |
| **Patient portal with opt-out** | PATIENT role, self-registration flow, per-patient SMS/email opt-out flags checked before every notification dispatch |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security 6 + JJWT 0.12 (stateless JWT) |
| Database | PostgreSQL 16 |
| Migrations | Flyway (V1–V13) |
| API Docs | SpringDoc OpenAPI 3.0 / Swagger UI |
| Validation | Jakarta Bean Validation |
| Build | Maven |

---

## Modules (14 phases shipped)

```
auth/          → Registration, login, JWT refresh, password reset, email verification
clinic/        → Clinic profiles, IANA timezone, business hours, closure dates, settings
doctor/        → Doctor profiles, weekly schedules, breaks, day overrides, leaves, treatment types
patient/       → Patient profiles, medical history, emergency contacts, portal account linking
appointment/   → Booking with 8-step validation, status lifecycle, reschedule, cancel
queue/         → Walk-in token queue, check-in, call/start/complete, wait time tracking
billing/       → Invoices, line items, discount+tax math, payment recording, refund
medical/       → Prescriptions, vitals, clinical notes
notification/  → SMS + email dispatch, preference management, 24h reminder scheduler
portal/        → Patient self-service (appointments, prescriptions, invoices, profile)
subscription/  → Plan tiers (FREE/PRO/ENTERPRISE), enforcement, platform admin dashboard
audit/         → Immutable change log with before/after JSON snapshots
reporting/     → Revenue, appointment, queue, patient, doctor performance analytics
```

---

## API — 91 endpoints across 17 controllers

| Controller | Endpoints | Auth |
|-----------|-----------|------|
| Authentication | 7 | Public |
| Clinics | 12 | Public reads, CLINIC_ADMIN writes |
| Doctors | 15 | Public reads, CLINIC_ADMIN writes |
| Patients | 6 | Authenticated |
| Appointments | 8 | Authenticated |
| Queue | 8 | CLINIC_ADMIN |
| Billing | 8 | CLINIC_ADMIN |
| Prescriptions | 5 | CLINIC_ADMIN |
| Vitals | 5 | CLINIC_ADMIN |
| Clinical Notes | 5 | CLINIC_ADMIN |
| Notifications | 4 | CLINIC_ADMIN |
| Patient Portal | 5 | PATIENT |
| Subscriptions | 2 | CLINIC_ADMIN |
| Plans | 1 | Public |
| Reports | 5 | CLINIC_ADMIN |
| Audit Logs | 1 | CLINIC_ADMIN / SUPER_ADMIN |
| Platform Admin | 2 | SUPER_ADMIN |

Full Postman-style docs for every endpoint: [`api.md`](./api.md)

---

## Running locally

### Prerequisites
- Java 21
- PostgreSQL 16 running on `localhost:5432`
- Database named `clinicos` created

```sql
CREATE DATABASE clinicos;
```

### Start

```bash
./mvnw spring-boot:run
```

The app starts on port `8080`. Flyway runs all 13 migrations automatically on first start.

### Environment variables (optional overrides)

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/clinicos` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `devxprakash` | Database password |
| `JWT_SECRET` | *(dev key in application.yaml)* | HS256 signing secret — rotate in production |
| `PORT` | `8080` | Server port |

### Interactive API docs

```
http://localhost:8080/swagger-ui/index.html
```

1. Hit `POST /api/v1/auth/register` to create a CLINIC_ADMIN account
2. Hit `POST /api/v1/auth/login` to get a JWT token
3. Click **Authorize** (top right of Swagger UI), paste the token
4. All endpoints are now available with one-click **Try it out**

---

## Database schema

13 Flyway migrations — Flyway runs them in order on startup:

| Migration | Tables created |
|-----------|---------------|
| V1 | Base config |
| V2 | `users`, `roles`, `user_roles`, `refresh_tokens` |
| V3 | `clinics`, `clinic_business_hours`, `clinic_closure_dates`, `clinic_settings` |
| V4 | `doctors`, `doctor_weekly_schedule`, `doctor_breaks`, `doctor_day_override`, `doctor_leave_dates`, `treatment_types`, `doctor_treatments` |
| V5 | `patients` |
| V6 | `appointments` |
| V7 | `queue_tokens` |
| V8 | `invoices`, `invoice_items`, `payments` |
| V9 | `prescriptions`, `prescription_medicines`, `vitals`, `clinical_notes` |
| V10 | `notification_logs`, `notification_preferences` |
| V11 | `clinic_subscriptions` + patient portal columns |
| V12 | `plans` (subscription tiers with pricing) |
| V13 | `audit_logs` |

---

## Roles & access control

| Role | What they can do |
|------|----------------|
| `SUPER_ADMIN` | Manage any clinic, view platform-wide revenue and clinic list |
| `CLINIC_ADMIN` | Manage their own clinic, doctors, patients, appointments, billing, records |
| `PATIENT` | Self-service portal — view own appointments, prescriptions, invoices |
| *(public)* | Read clinic/doctor listings, view plans |

---

## Upcoming phases

| Phase | What |
|-------|------|
| 16 | Test suite — JUnit 5, Testcontainers, JaCoCo coverage gate |
| 17 | Docker + docker-compose (single-command local setup) |
| 18 | CI/CD — GitHub Actions (test → build → deploy on every push) |
| 19 | Redis caching — availability algorithm, rate limiting |
| 20 | Real notification providers — Twilio SMS + SendGrid email |
| 21 | WebSocket real-time queue board (STOMP) |
| 22 | Razorpay subscription payment gateway |
| 23 | Agentic AI with OpenRouter — multi-tool clinical assistant |
| 24 | COBOL financial calculation engine |
| 25 | Observability — Actuator, Prometheus, Grafana |
| 26 | Production deployment — Railway + AWS reference architecture |

---

## Project structure

```
src/
└── main/
    ├── java/com/prakash/clinicos/
    │   ├── appointment/       controller, dto, entity, repository, service
    │   ├── audit/             controller, dto, entity, repository, service
    │   ├── auth/              controller, dto, entity, repository, service
    │   ├── billing/           controller, dto, entity, repository, service
    │   ├── clinic/            controller, dto, entity, repository, service
    │   ├── common/            BaseEntity, SlugUtils
    │   ├── config/            SecurityConfig, JpaConfig, OpenApiConfig
    │   ├── doctor/            controller, dto, entity, repository, service (×3)
    │   ├── exception/         AppException, GlobalExceptionHandler
    │   ├── medical/           controller, dto, entity, repository, service (×3)
    │   ├── notification/      controller, dto, entity, repository, scheduler, service, provider
    │   ├── patient/           controller, dto, entity, repository, service
    │   ├── portal/            controller, dto, service
    │   ├── queue/             controller, dto, entity, repository, service
    │   ├── reporting/         controller, dto, service
    │   ├── security/          JwtAuthenticationFilter, JwtTokenProvider, UserPrincipal
    │   └── subscription/      controller, dto, entity, repository, service (×2)
    └── resources/
        ├── application.yaml
        └── db/migration/      V1__…sql through V13__…sql
```

---

## Author

**Prakash Jha** — building ClinicOS as a showcase of production-grade Spring Boot architecture.
