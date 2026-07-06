# TODO.md - ClinicOS

## Phase 1 - Project Setup
- [ ] Create Spring Boot project (Java 21)
- [ ] Configure PostgreSQL
- [ ] Configure Flyway
- [ ] Configure Docker Compose
- [ ] Configure Spring Security
- [ ] Configure JWT
- [ ] Configure Swagger/OpenAPI
- [ ] Configure Global Exception Handler
- [ ] Configure Logging
- [ ] Configure GitHub Actions

## Phase 2 - Authentication ✅
- [x] User entity
- [x] Role entity
- [x] Register API
- [x] Login API
- [x] Refresh Token (with token rotation)
- [x] Forgot Password (mock – token returned in response)
- [x] Reset Password
- [x] Email Verification
- [x] RBAC (@EnableMethodSecurity + roles seeded in DB)
- [ ] Integration Tests (deferred to Phase 11)

## Phase 3 - Clinic
- [ ] Clinic CRUD
- [ ] Clinic Settings
- [ ] Upload Logo
- [ ] Business Hours

## Phase 4 - Doctor
- [ ] Doctor CRUD
- [ ] Availability
- [ ] Leave Management

## Phase 5 - Patient
- [ ] Patient CRUD
- [ ] Search
- [ ] Pagination
- [ ] Medical History

## Phase 6 - Appointments
- [ ] Slot generation
- [ ] Booking
- [ ] Cancel
- [ ] Reschedule
- [ ] Waiting list
- [ ] Optimistic locking
- [ ] Idempotency

## Phase 7 - Queue
- [ ] Queue tokens
- [ ] WebSocket updates
- [ ] Reception dashboard

## Phase 8 - Billing
- [ ] Invoice
- [ ] Payments
- [ ] PDF generation

## Phase 9 - Notifications
- [ ] RabbitMQ
- [ ] Email
- [ ] SMS mock
- [ ] WhatsApp mock

## Phase 10 - Widget
- [ ] Public booking APIs
- [ ] Widget configuration
- [ ] Embeddable script

## Phase 11 - Production
- [ ] Redis caching
- [ ] Audit logs
- [ ] Rate limiting
- [ ] Docker deployment
- [ ] Testcontainers
- [ ] API documentation
- [ ] Final resume screenshots
