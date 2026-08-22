# Multi-Talent SaaS Platform

A multi-tenant SaaS platform built as **true independent microservices** — each service is its own Spring Boot application with its own `pom.xml`, `application.yml`, main class, port, database, and Dockerfile. Services communicate over REST (service-to-service) and Kafka (event fan-out). A vanilla HTML/CSS/JS frontend talks to them all through nginx (locally) or a Kubernetes Ingress (in-cluster).

```
                     JavaScript (Browser)
                              │
                              ▼
                 nginx (local) / Ingress (k8s)      ← routes by /api/<service> path
                              │
        ┌──────────┬──────────┬──────────┬──────────┬──────────┐
        ▼          ▼          ▼          ▼          ▼          ▼
   auth-service tenant-  project-  email-    audit-    analytics-
   :8081        service  service   service   service   service
                :8082    :8083     :8084     :8085     :8086
        │          │          │        ▲         ▲          ▲
        │          │          │        │         │          │
        │◄─REST────┘          │        └─────Kafka events───┘
        │  (validate tenant)  │        (email-events, audit-events,
        │                     │         analytics-events)
        ▼                     ▼
     MySQL                 MySQL              MySQL: auth_db / tenant_db / project_db
    (auth_db)            (project_db)         MongoDB: audit_db
                                               Redis: tenant-service cache,
                                                      project-service cache,
                                                      analytics-service counters
```

## Services

| Service | Port | Own DB | Publishes to Kafka | Consumes from Kafka | Calls other services |
|---|---|---|---|---|---|
| **common-lib** | — | — | — | — | shared JWT util, event contracts, `ApiResponse`, exceptions — installed once, used by every service as a normal Maven dependency |
| **auth-service** | 8081 | MySQL `auth_db` | `email-events`, `audit-events`, `analytics-events` | — | `tenant-service` (REST, validates tenant slug on register) |
| **tenant-service** | 8082 | MySQL `tenant_db` (Redis cache) | — | — | — |
| **project-service** | 8083 | MySQL `project_db` (Redis cache) | `audit-events`, `analytics-events` | — | none — validates JWTs locally |
| **email-service** | 8084 | none | — | `email-events` | — |
| **audit-service** | 8085 | MongoDB `audit_db` | — | `audit-events` | — |
| **analytics-service** | 8086 | Redis counters | — | `analytics-events` | — |

Each service is independently buildable and runnable: `cd <service> && mvn spring-boot:run` (after installing `common-lib` once).

## Why this shape

- **auth-service** owns identity. It's the only service with a users table and the only one that checks passwords. It issues JWTs carrying `userId`, `tenantId`, and `role` as claims.
- **tenant-service** and **project-service** never call auth-service to "check who this is" — they decode the JWT locally (via `common-lib`'s `JwtAuthFilter`/`JwtUtil`, sharing the same signing secret) and trust its claims. That's what makes them scale independently without a session/auth service becoming a bottleneck.
- **auth-service → tenant-service** is the one synchronous service-to-service REST call in the system (confirming a tenant slug exists during registration).
- **email-service, audit-service, analytics-service** are pure Kafka consumers — they never get called directly by the frontend for writes, only reached asynchronously via events published by auth-service and project-service. `audit-service` and `analytics-service` also expose small read APIs so the frontend/an admin view can query their data.

## common-lib: a Maven project too, just a shared one

`common-lib` is a normal Maven project (own `pom.xml`, own `groupId:artifactId:version`) — it's just not a runnable service. Every other service declares it as a `<dependency>`:

```xml
<dependency>
    <groupId>com.multitalent</groupId>
    <artifactId>common-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

Before building any service standalone, install it once:

```bash
cd common-lib
mvn clean install
```

This publishes `common-lib-1.0.0.jar` to your local `~/.m2` repository. Every service's Dockerfile also does this automatically as its first build stage, so `docker build` / `docker compose up --build` works without any manual step.

What's in it: `JwtUtil` (issue/parse tokens), `JwtAuthFilter` (generic — builds an `AuthenticatedPrincipal` straight from JWT claims, no DB call), `AuthenticatedPrincipal`, Kafka event classes (`BaseEvent`, `UserRegisteredEvent`, `UserLoggedInEvent`, `ProjectCreatedEvent`), `KafkaTopics` constants, `EventProducer`, shared Kafka producer/consumer `@Configuration` classes, `ApiResponse<T>`, and the common exception types + `GlobalExceptionHandler`.

## Project layout

```
multi-talent-saas-platform/
├── common-lib/                 # shared jar — own pom.xml
├── auth-service/                # own pom.xml, application.yml, Dockerfile, port 8081
├── tenant-service/               # port 8082
├── project-service/              # port 8083
├── email-service/                 # port 8084
├── audit-service/                  # port 8085
├── analytics-service/               # port 8086
├── frontend/                    # static HTML/CSS/JS + nginx.conf + Dockerfile
├── k8s/                          # Kubernetes manifests (apply in numeric order)
├── mysql-init/                   # init script granting the shared DB user cross-database privileges
└── docker-compose.yml             # local dev — spins up every service + infra
```

## Running locally with Docker Compose

```bash
cd multi-talent-saas-platform
docker compose up --build
```

This builds and starts: MySQL, MongoDB, Redis, Zookeeper, Kafka, all 6 microservices, and the frontend (port `8888`).

Open **http://localhost:8888**, go to `register.html` to create your org + admin account, then use the dashboard.

Individual service ports are also exposed on the host if you want to hit them directly (e.g. `curl http://localhost:8081/api/auth/login`).

To tear down: `docker compose down -v`.

## Running a single service standalone (no Docker)

Requires Java 17, Maven, and the relevant infra (MySQL/MongoDB/Redis/Kafka) reachable — either via `docker compose up mysql mongodb redis kafka zookeeper` or your own instances.

```bash
# once
cd common-lib && mvn clean install && cd ..

# then, for whichever service you're working on
cd auth-service
mvn spring-boot:run
```

Each service's `application.yml` documents its own env vars with sensible localhost defaults. Every service needs the **same** `JWT_SECRET` value or tokens issued by auth-service won't validate elsewhere.

## Deploying to Kubernetes

1. **Build & push every service image** (repeat for each service — build context is the project root so each Dockerfile can reach `common-lib`):

```bash
docker build -f auth-service/Dockerfile        -t your-registry/auth-service:1.0.0        .
docker build -f tenant-service/Dockerfile      -t your-registry/tenant-service:1.0.0      .
docker build -f project-service/Dockerfile     -t your-registry/project-service:1.0.0     .
docker build -f email-service/Dockerfile       -t your-registry/email-service:1.0.0       .
docker build -f audit-service/Dockerfile       -t your-registry/audit-service:1.0.0       .
docker build -f analytics-service/Dockerfile   -t your-registry/analytics-service:1.0.0   .
docker build -t your-registry/multi-talent-saas-frontend:1.0.0 ./frontend

# push each one, then update the `image:` field in the matching k8s/2x-*.yaml
```

2. **Edit `k8s/01-shared-config.yaml`** and replace every placeholder credential in the `shared-secrets` Secret with real, securely-generated values.

3. **Apply the manifests in order:**

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-shared-config.yaml
kubectl apply -f k8s/10-mysql.yaml
kubectl apply -f k8s/11-mongodb.yaml
kubectl apply -f k8s/12-redis.yaml
kubectl apply -f k8s/13-kafka.yaml
kubectl apply -f k8s/20-auth-service.yaml
kubectl apply -f k8s/21-tenant-service.yaml
kubectl apply -f k8s/22-project-service.yaml
kubectl apply -f k8s/23-email-service.yaml
kubectl apply -f k8s/24-audit-service.yaml
kubectl apply -f k8s/25-analytics-service.yaml
kubectl apply -f k8s/26-frontend.yaml
kubectl apply -f k8s/30-ingress.yaml
```

4. **Check rollout:**

```bash
kubectl get pods -n multi-talent-saas -w
```

5. Point DNS / `/etc/hosts` at the Ingress controller's external IP for the host in `30-ingress.yaml` (`multitalent-saas.local` by default).

### Notes on the k8s setup

- MySQL, MongoDB, and Kafka use `PersistentVolumeClaim`s — for production, consider managed services (RDS, Atlas, MSK/Confluent Cloud) instead of self-hosting stateful services in-cluster.
- **Shared MySQL, separate logical databases**: all three MySQL-backed services (auth/tenant/project) connect to the *same* MySQL instance with the *same* admin user, but each uses its own database name (`auth_db`, `tenant_db`, `project_db`) via `createDatabaseIfNotExist=true`. `k8s/10-mysql.yaml` mounts an init script (`backend/mysql-init/01-grant-all-databases.sql`) granting that user cross-database privileges — this is a local/demo simplification; for production, prefer either least-privilege per-service DB users or fully separate DB instances per service.
- `project-service` has a `HorizontalPodAutoscaler` (2–6 replicas, scales on 70% CPU) as an example; add HPAs to other services the same way if needed.
- Liveness/readiness probes use Spring Boot Actuator's `/actuator/health/liveness` and `/actuator/health/readiness` on each service's own port.
- Single-broker Kafka — fine for demonstration, not for production (no replication).
- `email-service` has no Ingress path since it's a pure Kafka consumer with no public API.

## API overview

| Method | Path | Service | Auth | Description |
|---|---|---|---|---|
| POST | `/api/tenants` | tenant-service | No | Create a new tenant/workspace |
| GET | `/api/tenants/{slug}` | tenant-service | No | Get tenant by slug (also called by auth-service internally) |
| POST | `/api/auth/register` | auth-service | No | Register a user under a tenant, returns JWT |
| POST | `/api/auth/login` | auth-service | No | Login, returns JWT |
| GET/POST/PUT/DELETE | `/api/projects` | project-service | Yes (JWT) | Project CRUD, scoped to caller's tenant |
| GET | `/api/audit` | audit-service | Yes (JWT) | Audit trail for caller's tenant |
| GET | `/api/analytics/today` | analytics-service | Yes (JWT) | Today's event counters |

Authenticated requests need:
```
Authorization: Bearer <jwt-token>
```
(`tenantId` and `role` already travel inside the JWT claims — no separate `X-Tenant-Id` header needed in this version, since every resource service decodes the token itself.)

## Multi-tenancy model

Shared-database, tenant-scoped rows within each service's own database (`Project.tenantId`, etc.), with tenant identity carried end-to-end via JWT claims rather than re-validated against a database on every request.

## What to build next

- Role-based authorization (`@PreAuthorize`) using the `role` claim already present in every decoded JWT
- Refresh tokens / token revocation (needs a shared revocation store, e.g. Redis, if you want immediate logout)
- Circuit breaker (Resilience4j) around the auth-service → tenant-service REST call
- Distributed tracing (e.g. OpenTelemetry) — with 6 services, request tracing across the Kafka/REST boundaries becomes much more valuable
- Contract tests between producers/consumers of each Kafka topic (e.g. Spring Cloud Contract) since `common-lib` is now the single source of truth for event shapes
- CI/CD pipeline that builds/tests/pushes each service image independently
