# ClinicOS – API Reference

**Base URL:** `http://localhost:8080`
**Interactive docs:** `http://localhost:8080/swagger-ui/index.html`
**Auth:** `Authorization: Bearer <access_token>`

> All request/response schemas with live examples are available in Swagger UI. This file is a phase-by-phase endpoint index.

---

## Phase 2 — Authentication

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/register` | Public | Register a new CLINIC_ADMIN account |
| POST | `/api/v1/auth/login` | Public | Login and receive access + refresh tokens |
| POST | `/api/v1/auth/refresh` | Public | Exchange refresh token for new access token |
| POST | `/api/v1/auth/logout` | Bearer | Revoke refresh token |
| POST | `/api/v1/auth/forgot-password` | Public | Send password-reset email |
| POST | `/api/v1/auth/reset-password` | Public | Reset password using token from email |
| POST | `/api/v1/auth/verify-email` | Public | Verify email address using token from email |

---

## Phase 3 — Clinic Management

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/clinics` | CLINIC_ADMIN | Create a new clinic |
| GET | `/api/v1/clinics/mine` | CLINIC_ADMIN | Get the authenticated admin's clinic |
| GET | `/api/v1/clinics` | Public | List all clinics |
| GET | `/api/v1/clinics/{id}` | Public | Get clinic by ID |
| PUT | `/api/v1/clinics/{id}` | CLINIC_ADMIN | Update clinic profile |
| DELETE | `/api/v1/clinics/{id}` | CLINIC_ADMIN | Delete clinic |
| POST | `/api/v1/clinics/{id}/emergency-close` | CLINIC_ADMIN | Mark clinic as temporarily closed |
| POST | `/api/v1/clinics/{id}/emergency-reopen` | CLINIC_ADMIN | Reopen after emergency closure |
| GET | `/api/v1/clinics/{id}/hours` | Public | Get business hours |
| PUT | `/api/v1/clinics/{id}/hours` | CLINIC_ADMIN | Replace all business hours |
| PUT | `/api/v1/clinics/{id}/hours/{day}` | CLINIC_ADMIN | Update hours for a specific day |
| DELETE | `/api/v1/clinics/{id}/hours/{day}` | CLINIC_ADMIN | Remove hours for a specific day (mark closed) |
| GET | `/api/v1/clinics/{id}/closures` | Public | List closure dates |
| POST | `/api/v1/clinics/{id}/closures` | CLINIC_ADMIN | Add a closure date |
| DELETE | `/api/v1/clinics/{id}/closures/{cid}` | CLINIC_ADMIN | Remove a closure date |
| GET | `/api/v1/clinics/{id}/settings` | CLINIC_ADMIN | Get clinic settings |
| PUT | `/api/v1/clinics/{id}/settings` | CLINIC_ADMIN | Update clinic settings |

---

## Phase 4 — Doctor Management

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/clinics/{cid}/doctors` | CLINIC_ADMIN | Add a doctor to the clinic |
| GET | `/api/v1/clinics/{cid}/doctors` | Public | List all doctors in clinic |
| GET | `/api/v1/clinics/{cid}/doctors/{id}` | Public | Get doctor profile |
| PUT | `/api/v1/clinics/{cid}/doctors/{id}` | CLINIC_ADMIN | Update doctor profile |
| DELETE | `/api/v1/clinics/{cid}/doctors/{id}` | CLINIC_ADMIN | Remove doctor |
| PUT | `/api/v1/clinics/{cid}/doctors/{id}/link-user/{uid}` | CLINIC_ADMIN | Link doctor to a user account |
| DELETE | `/api/v1/clinics/{cid}/doctors/{id}/link-user` | CLINIC_ADMIN | Unlink doctor from user account |
| GET | `/api/v1/clinics/{cid}/doctors/{id}/schedule` | Public | Get weekly schedule |
| PUT | `/api/v1/clinics/{cid}/doctors/{id}/schedule` | CLINIC_ADMIN | Set full weekly schedule |
| DELETE | `/api/v1/clinics/{cid}/doctors/{id}/schedule` | CLINIC_ADMIN | Clear entire schedule |
| PUT | `/api/v1/clinics/{cid}/doctors/{id}/schedule/{day}` | CLINIC_ADMIN | Update schedule for a day |
| DELETE | `/api/v1/clinics/{cid}/doctors/{id}/schedule/{day}` | CLINIC_ADMIN | Remove schedule for a day |
| GET | `/api/v1/clinics/{cid}/doctors/{id}/breaks/{day}` | Public | Get breaks for a day |
| PUT | `/api/v1/clinics/{cid}/doctors/{id}/breaks/{day}` | CLINIC_ADMIN | Set breaks for a day |
| DELETE | `/api/v1/clinics/{cid}/doctors/{id}/breaks/{day}` | CLINIC_ADMIN | Clear breaks for a day |
| GET | `/api/v1/clinics/{cid}/doctors/{id}/overrides` | Public | List day overrides |
| PUT | `/api/v1/clinics/{cid}/doctors/{id}/overrides` | CLINIC_ADMIN | Set a day override |
| DELETE | `/api/v1/clinics/{cid}/doctors/{id}/overrides/{date}` | CLINIC_ADMIN | Remove a day override |
| GET | `/api/v1/clinics/{cid}/doctors/{id}/leave` | CLINIC_ADMIN | List leave dates |
| POST | `/api/v1/clinics/{cid}/doctors/{id}/leave` | CLINIC_ADMIN | Add a leave date |
| DELETE | `/api/v1/clinics/{cid}/doctors/{id}/leave/{lid}` | CLINIC_ADMIN | Remove a leave date |
| GET | `/api/v1/clinics/{cid}/doctors/{id}/availability` | Public | Get available slots for a date |
| GET | `/api/v1/clinics/{cid}/treatment-types` | Public | List treatment types |
| POST | `/api/v1/clinics/{cid}/treatment-types` | CLINIC_ADMIN | Add treatment type |
| DELETE | `/api/v1/clinics/{cid}/treatment-types/{tid}` | CLINIC_ADMIN | Remove treatment type |

---

## Phase 5 — Patient Management

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/clinics/{cid}/patients` | CLINIC_ADMIN | Register a new patient |
| GET | `/api/v1/clinics/{cid}/patients` | Authenticated | List patients in clinic |
| GET | `/api/v1/clinics/{cid}/patients/{id}` | Authenticated | Get patient profile |
| PUT | `/api/v1/clinics/{cid}/patients/{id}` | CLINIC_ADMIN | Update patient profile |
| DELETE | `/api/v1/clinics/{cid}/patients/{id}` | CLINIC_ADMIN | Remove patient |
| POST | `/api/v1/clinics/{cid}/patients/{id}/link-portal` | CLINIC_ADMIN | Link patient to a portal user account |

---

## Phase 6 — Appointments

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/clinics/{cid}/appointments` | Authenticated | Book an appointment |
| GET | `/api/v1/clinics/{cid}/appointments` | CLINIC_ADMIN | List clinic appointments |
| GET | `/api/v1/clinics/{cid}/appointments/{id}` | Authenticated | Get appointment details |
| PATCH | `/api/v1/clinics/{cid}/appointments/{id}/status` | CLINIC_ADMIN | Update appointment status |
| POST | `/api/v1/clinics/{cid}/appointments/{id}/cancel` | Authenticated | Cancel appointment |
| POST | `/api/v1/clinics/{cid}/appointments/{id}/reschedule` | Authenticated | Reschedule (creates new, marks old as RESCHEDULED) |
| GET | `/api/v1/clinics/{cid}/doctors/{did}/appointments` | CLINIC_ADMIN | List a doctor's appointments |
| GET | `/api/v1/clinics/{cid}/patients/{pid}/appointments` | Authenticated | List a patient's appointments |

---

## Phase 7 — Walk-in Queue

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/clinics/{cid}/queue/check-in` | CLINIC_ADMIN | Issue queue token to walk-in patient |
| GET | `/api/v1/clinics/{cid}/queue` | CLINIC_ADMIN | View today's full queue |
| GET | `/api/v1/clinics/{cid}/queue/doctor/{did}` | CLINIC_ADMIN | View queue for a specific doctor |
| PATCH | `/api/v1/clinics/{cid}/queue/{tid}/call` | CLINIC_ADMIN | Call the next patient |
| PATCH | `/api/v1/clinics/{cid}/queue/{tid}/start` | CLINIC_ADMIN | Mark consultation as started |
| PATCH | `/api/v1/clinics/{cid}/queue/{tid}/complete` | CLINIC_ADMIN | Mark consultation as complete |
| PATCH | `/api/v1/clinics/{cid}/queue/{tid}/skip` | CLINIC_ADMIN | Skip a patient in the queue |
| GET | `/api/v1/clinics/{cid}/queue/stats` | CLINIC_ADMIN | Get average wait / consultation times |

---

## Phase 8 — Billing

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/clinics/{cid}/invoices` | CLINIC_ADMIN | Create a draft invoice |
| GET | `/api/v1/clinics/{cid}/invoices` | CLINIC_ADMIN | List clinic invoices |
| GET | `/api/v1/clinics/{cid}/invoices/{id}` | CLINIC_ADMIN | Get invoice details |
| POST | `/api/v1/clinics/{cid}/invoices/{id}/issue` | CLINIC_ADMIN | Issue (finalise) invoice |
| POST | `/api/v1/clinics/{cid}/invoices/{id}/payments` | CLINIC_ADMIN | Record a payment |
| POST | `/api/v1/clinics/{cid}/invoices/{id}/cancel` | CLINIC_ADMIN | Cancel invoice |
| POST | `/api/v1/clinics/{cid}/invoices/{id}/refund` | CLINIC_ADMIN | Issue a refund |
| GET | `/api/v1/clinics/{cid}/patients/{pid}/invoices` | CLINIC_ADMIN | List invoices for a patient |

---

## Phase 9 — Medical Records

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/clinics/{cid}/appointments/{aid}/prescriptions` | CLINIC_ADMIN | Create prescription |
| GET | `/api/v1/clinics/{cid}/appointments/{aid}/prescriptions` | CLINIC_ADMIN | List prescriptions for appointment |
| GET | `/api/v1/clinics/{cid}/prescriptions/{id}` | CLINIC_ADMIN | Get prescription |
| PUT | `/api/v1/clinics/{cid}/prescriptions/{id}` | CLINIC_ADMIN | Update prescription |
| DELETE | `/api/v1/clinics/{cid}/prescriptions/{id}` | CLINIC_ADMIN | Delete prescription |
| POST | `/api/v1/clinics/{cid}/appointments/{aid}/vitals` | CLINIC_ADMIN | Record vitals |
| GET | `/api/v1/clinics/{cid}/appointments/{aid}/vitals` | CLINIC_ADMIN | Get vitals for appointment |
| GET | `/api/v1/clinics/{cid}/vitals/{id}` | CLINIC_ADMIN | Get vitals record |
| PUT | `/api/v1/clinics/{cid}/vitals/{id}` | CLINIC_ADMIN | Update vitals |
| DELETE | `/api/v1/clinics/{cid}/vitals/{id}` | CLINIC_ADMIN | Delete vitals |
| POST | `/api/v1/clinics/{cid}/appointments/{aid}/notes` | CLINIC_ADMIN | Add clinical note |
| GET | `/api/v1/clinics/{cid}/appointments/{aid}/notes` | CLINIC_ADMIN | List notes for appointment |
| GET | `/api/v1/clinics/{cid}/notes/{id}` | CLINIC_ADMIN | Get note |
| PUT | `/api/v1/clinics/{cid}/notes/{id}` | CLINIC_ADMIN | Update note |
| DELETE | `/api/v1/clinics/{cid}/notes/{id}` | CLINIC_ADMIN | Delete note |

---

## Phase 10 — Notifications

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/clinics/{cid}/notifications/send` | CLINIC_ADMIN | Manually send SMS/email notification |
| GET | `/api/v1/clinics/{cid}/notifications` | CLINIC_ADMIN | List notification logs |
| GET | `/api/v1/clinics/{cid}/patients/{pid}/notification-preferences` | CLINIC_ADMIN | Get patient notification preferences |
| PUT | `/api/v1/clinics/{cid}/patients/{pid}/notification-preferences` | CLINIC_ADMIN | Update patient notification preferences |

---

## Phase 11 — Patient Portal

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/portal/register` | Public | Self-register a PATIENT account |
| GET | `/api/v1/me/appointments` | PATIENT | View own appointments |
| GET | `/api/v1/me/prescriptions` | PATIENT | View own prescriptions |
| GET | `/api/v1/me/invoices` | PATIENT | View own invoices |
| PUT | `/api/v1/me/profile` | PATIENT | Update own profile |

---

## Phase 12 — Subscription Plans

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/plans` | Public | List available subscription plans |
| GET | `/api/v1/clinics/{cid}/subscription` | CLINIC_ADMIN | Get clinic's current subscription |
| POST | `/api/v1/clinics/{cid}/subscription/upgrade` | CLINIC_ADMIN | Upgrade subscription plan |

---

## Phase 13 — Audit Logs

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/clinics/{cid}/audit-logs` | CLINIC_ADMIN / SUPER_ADMIN | Query immutable audit trail |

---

## Phase 14 — Platform Admin & Reporting

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/admin/clinics` | SUPER_ADMIN | List all clinics on the platform |
| GET | `/api/v1/admin/revenue` | SUPER_ADMIN | Platform-wide revenue report |
| GET | `/api/v1/clinics/{cid}/reports/revenue` | CLINIC_ADMIN | Revenue report for clinic |
| GET | `/api/v1/clinics/{cid}/reports/appointments` | CLINIC_ADMIN | Appointment analytics |
| GET | `/api/v1/clinics/{cid}/reports/queue` | CLINIC_ADMIN | Queue performance analytics |
| GET | `/api/v1/clinics/{cid}/reports/patients` | CLINIC_ADMIN | Patient growth analytics |
| GET | `/api/v1/clinics/{cid}/reports/doctors` | CLINIC_ADMIN | Doctor performance analytics |

---

## Phase 15 — OpenAPI / Swagger UI

Interactive API documentation auto-generated from code annotations.

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/api-docs`
- All 91 endpoints documented with schemas, auth requirements, and live Try-it-out

---

## Phase 16 — Test Suite

JUnit 5 unit tests (Mockito) + Testcontainers integration tests + JaCoCo coverage gate.

- **Unit tests** — pure Mockito, no Spring context, no DB (`AuthServiceTest`, `AppointmentServiceTest`)
- **Integration tests** — real PostgreSQL via Testcontainers, full Spring Security filter chain (`AuthIntegrationTest`, `ClinicIntegrationTest`)
- **JaCoCo gate** — configured in `pom.xml`; run with `./mvnw verify`; report at `target/site/jacoco/index.html`
- Integration tests skipped cleanly when Docker is unavailable (`@Testcontainers(disabledWithoutDocker = true)`)

---

## Phase 17 — Docker

Single-command local setup via Docker Compose.

**Files added:**
- `Dockerfile` — multi-stage build: Maven+JDK21 builder → JRE21 Alpine runtime
- `docker-compose.yml` — postgres + app services with health checks
- `.dockerignore` — excludes `target/`, `.git/`, `.env`, logs, docs

**Commands:**

```bash
# Start everything (builds the app image on first run)
docker compose up --build

# Stop and remove containers (keep DB volume)
docker compose down

# Stop and wipe the DB volume (full reset)
docker compose down -v

# Rebuild the app image after code changes
docker compose build app && docker compose up -d app
```

**Services:**

| Service | Container | Port | Notes |
|---------|-----------|------|-------|
| postgres | clinicos-postgres | 5432 | postgres:16-alpine, health-checked |
| redis | clinicos-redis | 6379 | redis:7-alpine, health-checked (Phase 19) |
| app | clinicos-app | 8080 | waits for postgres + redis health before starting |
| prometheus | clinicos-prometheus | 9090 | scrapes `/actuator/prometheus` every 5s (Phase 25) |
| grafana | clinicos-grafana | 3000 | pre-provisioned Prometheus datasource + dashboard (Phase 25) |

After startup: `http://localhost:8080/swagger-ui/index.html`

---

## Phase 18 — CI/CD

GitHub Actions pipeline on every push/PR to `main` (`.github/workflows/ci.yml`):

1. Checkout + JDK 21 (Temurin) with Maven dependency caching
2. Install GnuCOBOL (`apt-get install gnucobol4`) and compile the billing engine (`scripts/compile-cobol.sh`)
3. `./mvnw verify` — unit + Testcontainers integration tests (real Docker on the runner) + JaCoCo coverage gate
4. Upload the JaCoCo HTML report as a build artifact
5. Package the jar and build the Docker image (validates the full multi-stage Dockerfile; not pushed anywhere — no registry configured)

---

## Phase 19 — Redis Caching

Redis-backed cache in front of `DoctorAvailabilityService.computeAvailability()` — the 11-step slot-generation algorithm that touches five tables on every call.

- **`RedisConfig`** — `doctorAvailability` cache, 30s TTL, JDK serialization (the response DTO is a Lombok `@Builder`-only class with no default constructor, so JSON round-tripping would need a hand-built Jackson deserializer; JDK serialization sidesteps that for an internal-only cache)
- **Eviction** — `AppointmentService` evicts the exact `(doctorId, date)` entry on book/cancel/reschedule; `DoctorScheduleService`/`DoctorService` evict the whole cache on schedule/override/leave writes (one change can affect many future dates)
- **Cache metrics** exposed at `/actuator/prometheus` (`cache_gets_total{cache="doctorAvailability",result="hit|miss"}`)
- Test profile uses `spring.cache.type: simple` (in-memory) — no Redis dependency for the test suite

---

## Phase 21 — WebSocket Queue Board

STOMP over WebSocket for live queue updates, replacing client-side polling.

- **Endpoint:** `ws://localhost:8080/ws` (SockJS fallback)
- **Topic:** `/topic/clinics/{clinicId}/queue` — clinic-scoped, so one tenant's queue events never reach another's screen
- `QueueService` publishes the changed token after every transition: generate, check-in, call, start, complete, skip, recall, cancel

---

## Phase 23 — Agentic AI Clinical Assistant

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/clinics/{cid}/ai/assistant` | CLINIC_ADMIN | Natural-language prompt → agentic tool-use loop |

Built on OpenRouter's OpenAI-compatible chat-completions API (`OpenRouterClient`, model configurable via `OPENROUTER_MODEL`, defaults to a free-tier model). `ClinicalAssistantService` runs a bounded (max 4 rounds) tool-use loop over four real backend tools:

- `search_patients` — resolve a name to a patientId
- `get_patient_appointment_history` — past/upcoming visits
- `check_doctor_availability` — free slots for a doctor/date
- `book_appointment` — books a real appointment via `AppointmentService`

`clinicId` is always injected server-side from the authenticated request path — no tool ever accepts it as a model-supplied argument, so the assistant can't be prompted into leaking another clinic's data. Prescription drafting is answered directly in the model's reply (medicines/diagnosis/instructions as text) rather than a persisting tool, so nothing gets written to a patient's chart without a doctor reviewing it first.

Returns `503` with a clear message if `OPENROUTER_API_KEY` isn't set — the rest of the app runs normally without it.

---

## Phase 24 — GnuCOBOL Billing Engine

`BillingService.createInvoice()` delegates discount/tax/total calculation to a compiled GnuCOBOL subprocess (`src/main/cobol/BillingCalc.cbl`) via `CobolBillingCalculator` — fixed-point COBOL decimal arithmetic instead of doing the money math in the JVM, over a stdin/stdout IPC protocol (fixed-width unsigned digit fields, implied 2-decimal scale).

- **Compile:** `./scripts/compile-cobol.sh` (needs `brew install gnucobol` / `apt-get install gnucobol4`) — not part of the default Maven lifecycle, so `mvn test`/`mvn package` work on a machine without GnuCOBOL installed
- **Fallback:** if the compiled binary is missing, times out, or errors, `BillingService` catches `CobolUnavailableException` and falls back to the equivalent Java `BigDecimal` calculation — invoices are correct either way
- **Docker:** the builder stage installs GnuCOBOL and compiles the binary; the runtime stage installs `gnucobol` for `libcob.so` and copies the compiled binary in
- Verified against the real subprocess in `CobolBillingCalculatorTest` (skips gracefully if the binary isn't compiled locally)

---

## Phase 25 — Observability

Spring Actuator + Micrometer + Prometheus + Grafana.

- **`/actuator/prometheus`** — publicly exposed for scraping (health/info/metrics/prometheus endpoints)
- **p50/p95/p99 request latency** — `management.metrics.distribution.percentiles-histogram.http.server.requests`
- **JVM heap, HikariCP pool saturation, Redis cache hit/miss** — all auto-bound Micrometer meters, no extra code
- **Grafana** — pre-provisioned on startup (`monitoring/grafana/provisioning/`): Prometheus datasource + a "ClinicOS – Service Overview" dashboard (request latency, JVM heap, HikariCP saturation, request rate by status)

Access after `docker compose up`: Grafana at `http://localhost:3000` (admin / `GRAFANA_ADMIN_PASSWORD`, defaults to `admin`), Prometheus at `http://localhost:9090`.

---

## Upcoming Phases

Require a third-party account to go beyond a stub/mock — deferred until API keys are available.

| Phase | What | Needs |
|-------|------|-------|
| 20 | **Real Notifications** — Twilio SMS + SendGrid email providers | Twilio + SendGrid accounts |
| 22 | **Razorpay Gateway** — Subscription payment collection | Razorpay account (test mode is free) |
| 26 | **Production Deployment** — Railway + AWS reference architecture | Railway/AWS account |
