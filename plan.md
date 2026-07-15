# ClinicOS – 30-Day Development Plan

## Goal
Build a production-looking **ClinicOS – Multi-Tenant Clinic Management Platform** in one month that demonstrates backend engineering skills with Spring Boot.

---

# Tech Stack

## Backend
- Spring Boot 3
- Java 21
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- RabbitMQ
- Flyway
- Docker
- JWT
- Swagger / OpenAPI
- JUnit
- Testcontainers

## Frontend
- Next.js
- Tailwind CSS
- React Query
- React Hook Form
- shadcn/ui

---

# Week 1

## Authentication
- Login
- Register
- Refresh Token
- Forgot Password
- Email Verification
- JWT Authentication
- RBAC

### Roles
- Super Admin
- Clinic Admin
- Doctor
- Receptionist

## Clinic Module
- Create Clinic
- Update Clinic
- Delete Clinic
- Get Clinic
- Upload Logo
- Business Hours

## Doctor Module
- CRUD
- Availability
- Specialization
- Working Hours
- Leave Management

## Patient Module
- CRUD
- Medical History
- Search
- Filtering
- Pagination

**Milestone:** Functional multi-tenant clinic SaaS foundation.

---

# Week 2

## Appointment System
- Book Appointment
- Cancel Appointment
- Reschedule
- Doctor Availability
- Time Slots
- Recurring Slots
- Waiting List

## Queue Management
- Generate Token
- Current Token
- Completed
- Skipped
- Call Patient
- WebSocket Live Updates

## Reception Dashboard
- Today's Queue
- Walk-in Patient
- Search Patient
- Book Appointment

## Doctor Dashboard
- Today's Appointments
- Upcoming Appointments
- Queue
- Patient Details
- Prescription
- Medical Notes

---

# Week 3

## Billing
- Invoice
- Payment
- Tax
- Discount
- Refund

## Prescription
- Generate PDF
- Medicines
- Diagnosis
- Instructions

## Notifications
- RabbitMQ
- Email
- SMS (Mock)
- WhatsApp (Mock)

## Redis
- Cache Doctor List
- Cache Availability
- Cache Specializations

## Dashboard
- Today's Revenue
- Today's Patients
- Monthly Revenue
- Popular Doctors
- Cancelled Appointments

---

# Week 4

## Embeddable Booking Widget
```html
<script src="clinic-widget.js"></script>
```

### Widget Features
- Book Appointment
- Doctor Selection
- Calendar
- Payment
- OTP
- Confirmation
- Cancel
- Reschedule

### Widget Settings
- Enable Payments
- Allow Doctor Selection
- Working Hours
- Theme
- Logo
- Primary Color
- Language

---

# Production Features
- Audit Logs
- Activity History
- Soft Delete
- Optimistic Locking
- Rate Limiting
- Global Exception Handler
- Pagination
- Filtering
- Sorting
- Validation
- Docker
- Swagger
- Flyway
- Structured Logging

---

# Folder Structure

```
clinicos/
├── auth
├── clinic
├── doctor
├── patient
├── appointment
├── billing
├── queue
├── notification
├── widget
├── dashboard
├── audit
├── common
├── config
├── security
├── exception
└── util
```

---

# API Targets

- Auth – 12
- Clinics – 15
- Doctors – 20
- Patients – 20
- Appointments – 30
- Billing – 15
- Queue – 10
- Widget – 15
- Dashboard – 10
- Notifications – 8

Target: **~150 REST APIs**

---

# Database Tables

- users
- roles
- permissions
- clinics
- clinic_settings
- doctors
- doctor_availability
- patients
- appointments
- appointment_history
- queue_tokens
- prescriptions
- medicines
- prescription_items
- invoices
- invoice_items
- payments
- notifications
- audit_logs
- refresh_tokens
- files
- widget_settings

---

# Startup-Quality Features

- Dashboard
- Calendar View
- Queue Screen
- Appointment Timeline
- Live Search
- Keyboard Shortcuts
- Dark Mode
- Global Search
- Command Palette (Ctrl+K)
- Toast Notifications
- Skeleton Loaders
- Role-based Sidebar

---

# Engineering Highlights

- Idempotent appointment booking
- Optimistic locking
- Redis caching
- RabbitMQ notifications
- WebSocket live queue
- Flyway migrations
- Testcontainers
- Docker Compose
- OpenAPI / Swagger
- GitHub Actions CI
- RFC 7807 Problem Details
- Request/Response logging with Trace IDs

---

# Resume Entry

ClinicOS — Multi-Tenant Clinic Management SaaS | Personal Project | 2025–Present
Spring Boot 4 · Java 21 · PostgreSQL · Redis · WebSocket/STOMP · Docker · GitHub Actions · OpenRouter AI · GnuCOBOL · Razorpay · Prometheus · Grafana · Spring Security · JUnit 5 · Testcontainers

- Architected a production-grade multi-tenant SaaS platform from scratch across 26 feature phases, delivering 150+ REST endpoints covering authentication, appointment scheduling, walk-in queue management, billing, medical records,
  patient portal, subscriptions, and analytics — with full end-to-end ownership from Flyway DB schema design through Dockerised deployment.
- Eliminated cross-tenant data leakage across all 150+ endpoints by implementing stateless JWT authentication (JJWT 0.12.6) with 3-tier RBAC (CLINIC_ADMIN, PATIENT, SUPER_ADMIN) and per-clinic request scoping enforced at the
  service layer, making unauthorized cross-tenant access structurally impossible by design.
- Enforced code quality at every merge by configuring a JaCoCo coverage gate in a GitHub Actions CI/CD pipeline, backed by JUnit 5 unit tests (Mockito strict mode) and Testcontainers integration tests running against a real
  PostgreSQL container — covering critical booking validation, auth flows, and appointment status-machine transitions with zero mocking of the database.
- Reduced scheduling query load and eliminated client polling by implementing Redis-backed caching for doctor availability computations and replacing repeated GET calls with STOMP WebSocket push events for live queue board updates
  — improving responsiveness under concurrent booking windows.
- Built an agentic AI clinical assistant using the OpenRouter multi-model API with structured tool-use, enabling natural-language appointment booking, patient history queries, and prescription drafts from a single prompt — applying
  LLM orchestration patterns used in production healthtech products.
- Integrated a GnuCOBOL billing subprocess with the Spring Boot backend for financial calculations, pairing COBOL's IEEE-standard decimal precision with a modern REST service — a cross-language IPC architecture used in banking
  cores, brought into a healthcare SaaS context.
- Containerised the full stack with a multi-stage Dockerfile (Maven builder → JRE 21 Alpine runtime) and Docker Compose, then automated a GitHub Actions pipeline (lint → test → build → Docker image publish) so every push to main
  produces a tested, deployment-ready image.
- Instrumented the platform with Spring Actuator, Prometheus metric scraping, and Grafana dashboards tracking p50/p95 request latency, JVM heap, and HikariCP connection pool saturation — enabling sub-minute incident detection in a
  production observability setup.

"Financial calculations are where most bugs cause real money loss. COBOL was designed specifically for decimal arithmetic precision — it's what banks run. I wanted to understand why those systems exist and how to bridge them with
modern APIs, so I built a GnuCOBOL subprocess that my Spring Boot service calls over IPC. It's the same architectural pattern the big banking cores use."
