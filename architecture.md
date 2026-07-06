# architecture.md - ClinicOS

## Architecture
- Frontend: Next.js
- Backend: Spring Boot Modular Monolith
- Database: PostgreSQL
- Cache: Redis
- Messaging: RabbitMQ
- File Storage: MinIO/S3 (optional)
- Auth: JWT + RBAC

## Modules
auth/
clinic/
doctor/
patient/
appointment/
queue/
billing/
notification/
widget/
audit/
dashboard/
common/

## Request Flow
Client -> Spring Security -> Controller -> Service -> Repository -> PostgreSQL

Async:
API -> RabbitMQ -> Notification Worker -> Email/SMS

## Database (Core)
users, roles, permissions, clinics, doctors, patients,
appointments, queue_tokens, prescriptions, invoices,
payments, notifications, audit_logs, refresh_tokens.

## Coding Rules
- DTOs only across controller boundary
- Service layer contains business logic
- Repository only for DB access
- Validation with Bean Validation
- Global exception handling
- Flyway for schema changes
- Constructor injection only
- Pagination on list APIs
- OpenAPI documentation for every endpoint

## Resume Highlights
- Multi-tenant SaaS
- JWT & RBAC
- Redis cache
- RabbitMQ async processing
- WebSocket live queue
- Dockerized deployment
- 150+ REST APIs
