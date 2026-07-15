# ClinicOS — Interview Q&A Prep

20 questions a technical interviewer is likely to ask off this resume bullet set, with answers
grounded in the actual implementation (file/class names included so you can navigate there live
if asked to show code).

---

### 1. Walk me through the multi-tenancy model. How do you actually prevent one clinic from seeing another clinic's data?

Every table that holds tenant data carries a `clinic_id` column, and every repository query that
touches patient/appointment/billing data is scoped by it — not just at the controller layer, at
the service layer. For example, `AppointmentService.bookAppointment()` loads the doctor and
patient and explicitly checks `doctor.getClinicId().equals(clinicId)` before doing anything else.
The `clinicId` itself always comes from the URL path (`/api/v1/clinics/{clinicId}/...`), resolved
against the authenticated user's own clinic — a user can never pass a clinic ID and get another
tenant's data back, because every read and write re-validates ownership before touching the row.

### 2. Why JWT instead of session-based auth?

Statelessness. With sessions, the server has to store session state somewhere (in memory or a
shared store like Redis), which complicates horizontal scaling — every instance needs access to
that store. With JWT, the token itself carries the claims (user id, roles), so any instance can
validate a request with just the signing secret. `SecurityConfig` sets
`SessionCreationPolicy.STATELESS` explicitly for this reason. The trade-off is you can't
instantly revoke a single access token — I handle that with short-lived (15 min) access tokens
plus a separate refresh-token table that *can* be revoked server-side.

### 3. Explain the RBAC setup — how are the three roles actually enforced?

`CLINIC_ADMIN`, `PATIENT`, and `SUPER_ADMIN` are enforced with `@PreAuthorize` at the endpoint
level — e.g. `@PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")` on write endpoints like
queue token transitions or invoice creation. `@EnableMethodSecurity` is turned on in
`SecurityConfig` so these annotations are actually evaluated. Roles are baked into the JWT as
claims at login time and reconstructed as Spring Security `GrantedAuthority` objects in
`JwtAuthenticationFilter`, so authorization checks don't need a DB hit per request.

### 4. "Structurally impossible by design" is a strong claim — is it actually true, or just difficult?

It's true in the sense that the check isn't optional or something a future developer could forget
— it's baked into the query/service pattern every endpoint follows. But I'll be honest under
follow-up: it's enforced by convention and code review, not by something like row-level security
in Postgres. If someone wrote a new service method and genuinely forgot the clinic-ownership
check, it would compile and pass most tests. The honest framing is "structurally consistent
across every existing endpoint," not "physically impossible at the database level." I did learn
this lesson while building the AI assistant tools in Phase 23 — one of the four tools
(`check_doctor_availability`) called a service method that *didn't* take `clinicId` as a
parameter at all, so I had to add an explicit ownership check in the tool itself before calling
it. That's a good example of where the pattern isn't automatic — it has to be actively applied
every time a new entry point is added.

### 5. Walk me through your CI/CD pipeline.

GitHub Actions, triggered on push/PR to `main` (`.github/workflows/ci.yml`). It checks out the
code, sets up JDK 21 with Maven caching, installs GnuCOBOL and compiles the billing engine, then
runs `./mvnw verify` — which runs the full JUnit test suite including Testcontainers integration
tests (GitHub's `ubuntu-latest` runners have Docker preinstalled, so Testcontainers spins up a
real PostgreSQL container), and the JaCoCo coverage gate. It uploads the coverage report as a
build artifact, then packages the jar and builds the Docker image to validate the whole
multi-stage Dockerfile actually builds. It doesn't push the image anywhere — no container
registry configured — so the last step is really "prove this Dockerfile is not broken."

### 6. Why Testcontainers instead of just mocking the database in tests?

Because mocked-DB tests can pass while the real SQL is broken — wrong join, constraint violation,
a query that works against H2 but not real Postgres. Testcontainers spins up an actual
`postgres:16-alpine` container per test class, runs all 13 Flyway migrations against it, and the
tests hit real SQL through the real Hibernate/JPA stack. It's slower than mocking, but it catches
an entire category of bugs mocks structurally can't catch. I do still have pure-Mockito unit
tests (`AppointmentServiceTest`, `AuthServiceTest`) for business-rule logic where a DB round trip
adds nothing — the split is deliberate: Mockito for logic, Testcontainers for anything that
touches SQL.

### 7. What's JaCoCo actually gating, and what's the threshold?

It's a Maven plugin bound to the `verify` phase that measures instruction coverage and fails the
build if it drops below a configured minimum, excluding DTOs, entities, config classes, and the
global exception handler (things where coverage is either meaningless or just noise). Full
disclosure if asked: the current threshold is set low (3%) as a baseline while the codebase adds
tests — the`pom.xml` comment on it explicitly notes the target is 80%. I'd rather be upfront that
the gate exists and works mechanically, and that raising the number is ongoing, than imply
coverage is where it needs to be.

### 8. Why Redis for the availability cache — what specifically is expensive that it's solving?

`DoctorAvailabilityService.computeAvailability()` runs an 11-step algorithm — checking clinic
closures, doctor leave, day overrides, weekly schedule, breaks, and every already-booked
appointment for that doctor/date — hitting five different tables. Under concurrent traffic (a
booking widget polling availability, multiple receptionists checking the same doctor) that same
`(doctorId, date)` pair gets recomputed identically, repeatedly, within seconds. I cache the
result in Redis for 30 seconds, keyed on `doctorId + date`.

### 9. Tell me about a real bug you hit building this — not a hypothetical, something that actually broke.

Good one, actually happened: I initially serialized the cached `DoctorAvailabilityResponse` as
JSON via Jackson. First request (cache miss) worked fine. Second request (cache hit) threw a 500
— `Cannot construct instance of DoctorAvailabilityResponse (no Creators, like default
constructor, exist)`. The class uses Lombok's `@Builder`, which — when there's no other explicit
constructor — generates an all-args constructor and, critically, *suppresses* Java's implicit
default no-arg constructor. Jackson can serialize a builder-pattern object fine (it just calls
getters), but it can't *deserialize* one without either a default constructor or an explicit
`@JsonDeserialize(builder = ...)` annotation pointing at the generated builder class. Rather than
chase that annotation across a Jackson 2/3 compatibility boundary this project happens to
straddle (Spring Boot 4 moved to `tools.jackson`, but a couple of transitive deps still pull in
old `com.fasterxml.jackson`), I made the DTO `Serializable` and switched that one cache to plain
JDK serialization instead — simpler, no annotation gymnastics, and it's an internal cache, not a
public API contract, so the binary format doesn't matter to any consumer.

### 10. How does eviction work — what happens if a slot gets booked right after it was cached?

Two layers. `AppointmentService` explicitly evicts the exact `(doctorId, date)` cache entry
immediately after book/cancel/reschedule — so a just-booked slot can never appear as free to the
next request. Schedule/override/leave changes in `DoctorScheduleService`/`DoctorService` evict
the *whole* cache, since one schedule change can ripple across many future dates and isn't worth
tracking precisely. The 30-second TTL is a backstop, not the primary correctness mechanism — it's
there in case a future write path is added and someone forgets to add explicit eviction.

### 11. Why WebSocket instead of just polling for the queue board?

Polling means every open reception screen sends a request every few seconds whether or not
anything changed — wasted load that scales with the number of open screens, not the number of
actual queue events. With STOMP over WebSocket, `QueueService` pushes exactly one message per
state transition (token generated, called, started, completed, skipped, recalled, cancelled) to
`/topic/clinics/{clinicId}/queue`, and every subscribed screen updates instantly. The topic is
clinic-scoped specifically so one tenant's queue events can never reach another tenant's open
screen.

### 12. Explain the architecture of the agentic AI assistant. How does "tool use" actually work under the hood?

It's built on OpenRouter's chat-completions API, which is OpenAI-compatible, so it supports
function/tool calling. `ClinicalAssistantService` sends the user's prompt plus a system prompt
and a list of tool definitions (JSON schemas) to the model. If the model responds with
`tool_calls` instead of plain text, I execute the matching Java tool — `search_patients`,
`get_patient_appointment_history`, `check_doctor_availability`, or `book_appointment` — against
the real backend, serialize the result back as a "tool" role message, and send the whole
conversation back to the model. That loop repeats — bounded at 4 rounds so a confused model can't
loop forever and rack up API cost — until the model returns a plain-text final answer.

### 13. How do you stop the AI from leaking one clinic's data into another clinic's conversation?

The model never supplies `clinicId` as a tool argument — it's injected server-side from the
authenticated request path before any tool executes, same as every other controller in the app.
I also had to add a manual ownership check inside `check_doctor_availability` specifically,
because the underlying `computeAvailability(doctorId, date)` method doesn't validate the doctor
belongs to the caller's clinic on its own — so if I'd trusted the model's `doctorId` blindly, a
hallucinated or malicious ID could return another clinic's schedule. That's the interface's
whole design contract: `ClinicalTool.execute()` takes `clinicId` from the orchestrator, never
from the parsed arguments.

### 14. If someone asks the assistant to draft a prescription, does it get saved to the patient's chart automatically?

No, deliberately not. There's no `draft_prescription` tool at all — the system prompt instructs
the model to write the draft (medicines, diagnosis, instructions) directly in its text reply. It
never touches the database. That's a patient-safety call: an LLM-generated medical order
shouldn't be persisted without a licensed doctor reviewing and explicitly entering it themselves.

### 15. Why GnuCOBOL for billing math — doesn't Java's BigDecimal already do exact decimal arithmetic?

Fair challenge, and yes — BigDecimal is exact fixed-point, same numeric result as COBOL's DISPLAY
numerics here. This wasn't solving a correctness gap Java had; it's demonstrating the
cross-language IPC pattern production banking cores actually use, where COBOL owns the financial
calculation and everything else calls into it over a process boundary. `BillingService` sends
subtotal/discount/tax as fixed-width unsigned digit fields over stdin to a compiled COBOL binary,
which computes discount, tax, and total using `COMPUTE ... ROUNDED`, and writes the result back
over stdout. I validate discount-doesn't-exceed-subtotal in Java *before* invoking it, because
COBOL's unsigned `PIC 9` fields would silently misbehave on a subtraction that goes negative.

### 16. What happens in production if the machine doesn't have GnuCOBOL installed?

`CobolBillingCalculator` checks the binary exists and is executable before invoking it. If it's
missing, times out (5s), or the process exits non-zero, it throws `CobolUnavailableException`,
and `BillingService` catches that specific exception and falls back to the equivalent Java
`BigDecimal` calculation — same formula, same rounding mode. Invoices come out correct either
way; only the code path differs. I did this on purpose so `mvn test`/`mvn package` work on a
laptop without GnuCOBOL installed — the compile step (`scripts/compile-cobol.sh`) is intentionally
*not* wired into the default Maven lifecycle. CI and the Docker build install GnuCOBOL explicitly
and always exercise the real subprocess path.

### 17. Walk me through the actual wire protocol between Java and the COBOL process.

Fixed-width, no delimiters, no headers — a deliberate choice because COBOL is much better at
fixed-column PIC fields than parsing delimited text. Every numeric field is unsigned digits with
an implied 2-decimal scale — e.g. `150.05` encodes as `000015005` for a 7-integer-digit field.
The Java side writes one line to the process's stdin (subtotal, discount%, discount-amount,
tax%), the COBOL program reads it into a `01` group item whose sub-fields are directly typed as
`PIC 9(7)V99`, computes discount/tax/total with `COMPUTE ... ROUNDED`, and writes a fixed-width
result line to stdout that the Java side decodes back into `BigDecimal`.

### 18. What metrics are you tracking with Prometheus/Grafana, and why those specifically?

p50/p95/p99 HTTP request latency (via Micrometer's percentile histograms on
`http.server.requests`), JVM heap usage, and HikariCP connection pool saturation — active vs
pending vs max connections. Those three were chosen because they're the standard "is the service
healthy" triad: request latency tells you if users are having a bad experience, heap tells you if
you're heading toward an OOM, and HikariCP pool saturation is usually the first symptom of a
connection leak or a slow query starving the pool before anything else visibly breaks. I also get
Redis cache hit/miss rate for free from Micrometer's cache metrics binder, which doubles as a
sanity check that the availability cache is actually doing something under load.

### 19. Walk me through the Docker setup.

Multi-stage build. Stage one is `maven:3.9-eclipse-temurin-21-alpine`, which resolves
dependencies, compiles, packages the jar, and also installs GnuCOBOL to compile the billing
engine — so the image build is the one place both the JVM and COBOL toolchains are guaranteed to
match what actually ships. Stage two is a bare `eclipse-temurin:21-jre-alpine` runtime image that
only gets the compiled jar, the compiled COBOL binary, and `gnucobol` installed just for its
runtime shared library (`libcob.so`) — no Maven, no JDK, no COBOL compiler in the final image, to
keep it lean. It runs as a non-root user. Docker Compose wires up Postgres, Redis, the app,
Prometheus, and Grafana with health checks so the app doesn't start before its dependencies are
actually ready.

### 20. If you rebuilt this from scratch, what would you do differently — or what's the weakest part right now?

Two honest answers. First, the JaCoCo coverage threshold is set low as a baseline — that's real
debt, not just a formality; I'd want meaningfully higher coverage before calling this
production-ready. Second, the "structurally impossible" tenant isolation claim (question 4) is
enforced by a consistent pattern across services, not by something mechanical like Postgres
row-level security — if I rebuilt it, I'd want that check enforced closer to the data layer
itself (e.g. a base repository that always requires a tenant filter) so it's not something a new
service method could accidentally skip. On the "what breaks first under load" front: the
WebSocket broker is currently Spring's in-memory `SimpleBroker`, which only works for a single
app instance — scaling to multiple instances would need a real STOMP relay like RabbitMQ so
messages fan out across instances correctly.
