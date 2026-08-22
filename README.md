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


