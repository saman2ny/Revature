# Fleet AI Platform

A production-shaped implementation of the fleet monitoring / driver-safety
architecture: event-driven telemetry ingestion, a five-agent AI layer
(Monitoring, Route, Alert, Approval, Conversation agents), RAG-grounded
supervisor chat, and real-time push to both a supervisor dashboard and a
driver mobile app.

See [`docs/architecture.md`](docs/architecture.md) for the full data-flow
diagram and the reasoning behind each design decision.

## Stack

| Layer | Choice |
|---|---|
| Backend services | Java 17, Spring Boot 3.2, Spring Cloud Gateway, Spring Kafka, Spring AI |
| AI / agents | Spring AI + Anthropic (Claude), pgvector-backed RAG, MCP client support |
| Event backbone | Apache Kafka |
| Data | PostgreSQL (+ pgvector), Redis, MinIO (S3-compatible) |
| Frontends | Angular 17 (supervisor-dashboard, driver-mobile-app) |
| Infra | Docker Compose (local), Kubernetes manifests (production), GitHub Actions (CI/CD) |

## Project structure

```
fleet-ai-platform/
├── docker-compose.yml          # full local stack — infra + all services + both frontends
├── services/
│   ├── api-gateway/                   # edge routing, JWT auth, telemetry -> Kafka ingestion
│   ├── monitoring-service/            # rule + AI anomaly detection, driver alerts
│   ├── route-optimization-service/    # candidate routes, Route Agent judgment
│   ├── ai-agent-service/              # the 5 agents + orchestrator + RAG (the AI Agent Layer)
│   └── notification-alert-service/    # Alert/Approval agent integration, WebSocket push
├── frontend/
│   ├── supervisor-dashboard/          # live feed, approvals queue, agent chat (Angular)
│   └── driver-mobile-app/             # telemetry simulator + personal alerts (Angular)
├── infra/k8s/                  # namespace, configmap, per-service Deployment/Service, ingress, HPA
├── .github/workflows/          # ci.yml (build+test every service/app), cd.yml (build, push, deploy)
├── scripts/                    # Kafka topic bootstrap, Postgres init (incl. pgvector extension)
└── docs/architecture.md
```

## Running it locally

```bash
cp .env.example .env
# edit .env — at minimum set ANTHROPIC_API_KEY for the AI Agent Layer to work

docker compose up -d --build
```

This starts, in order of readiness: Zookeeper, Kafka (+ topic bootstrap),
Postgres (with pgvector enabled and seed data), Redis, MinIO, then the five
Spring Boot services, then both Angular apps served via nginx.

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Supervisor Dashboard | http://localhost:8090 |
| Driver Mobile App | http://localhost:8091 |
| MinIO Console | http://localhost:9001 |
| Kafka broker | localhost:9092 |

Open the driver app, tap the telemetry toggle **ON** — within a few cycles
you'll see alerts and route recommendations flow through to the supervisor
dashboard's live feed, driven by the Monitoring and Route agents.

To also run Prometheus/Grafana: `docker compose --profile observability up -d`.

## Building each service standalone

```bash
cd services/<service-name>
mvn clean package
```

**Note on this environment:** this repo was authored without outbound network
access, so `mvn`/`npm` have not been run here to verify a clean build. Dependency
versions (especially the Spring AI milestone version pinned in
`ai-agent-service/pom.xml`) should be checked against current releases the
first time you build. Everything is structured to compile against a standard
Spring Boot 3.2 / Spring AI toolchain — treat the first `mvn clean install` on
each service as the actual build verification step.

## Deploying to Kubernetes

```bash
cp infra/k8s/secrets.yaml.example infra/k8s/secrets.yaml
# fill in real values — POSTGRES_PASSWORD, JWT_SECRET, ANTHROPIC_API_KEY, MinIO creds
kubectl apply -f infra/k8s/secrets.yaml

kubectl apply -f infra/k8s/namespace.yaml
kubectl apply -f infra/k8s/configmap-common.yaml
kubectl apply -R -f infra/k8s/postgres -f infra/k8s/redis -f infra/k8s/kafka -f infra/k8s/minio
kubectl apply -R -f infra/k8s/ai-agent-service -f infra/k8s/monitoring-service \
               -f infra/k8s/route-optimization-service -f infra/k8s/notification-alert-service \
               -f infra/k8s/api-gateway -f infra/k8s/supervisor-dashboard -f infra/k8s/driver-mobile-app
kubectl apply -f infra/k8s/ingress.yaml
```

`.github/workflows/cd.yml` automates exactly this sequence on push to `main`,
building and pushing each service's image to GHCR first.

## The AI Agent Layer

| Agent | Lives in | Job |
|---|---|---|
| Monitoring Agent | ai-agent-service | Judges a telemetry window: normal / anomaly / safety-critical |
| Route Agent | ai-agent-service | Weighs a candidate reroute against ETA gain vs. disruption/HOS/delivery windows |
| Alert Agent | ai-agent-service | Decides notification wording, audience, and whether to suppress alert-fatigue noise |
| Approval Agent | ai-agent-service | Human-in-the-loop gate: auto-approve within policy, or escalate to a supervisor |
| Conversation Agent | ai-agent-service | RAG-grounded chat for supervisors, cited against policy/incident documents in pgvector |

All five are called over REST from the services that need them
(`monitoring-service`, `route-optimization-service`, `notification-alert-service`),
so prompts, models, and RAG grounding are versioned and evaluated in exactly
one place. See [`docs/architecture.md`](docs/architecture.md) for why this is
structured as a separate service rather than embedded per-consumer.

## Roadmap / what a real production hardening pass would add next

- Swap the single-broker Kafka and single-instance Postgres for the Strimzi
  Kafka Operator and a managed/operator-backed Postgres (multi-AZ, automated
  failover).
- OAuth2/OIDC at the gateway instead of the shared-secret JWT validator.
- A real ingestion pipeline for the Conversation Agent's knowledge base
  (replacing the demo `KnowledgeIngestionService` seed data) — batch-chunking
  the client's actual driver handbooks and incident reports from MinIO/S3.
- Schema registry (Avro/Protobuf) for the Kafka event contracts instead of
  plain JSON, once more consumers depend on them.
- Distributed tracing (OpenTelemetry) across the REST + Kafka hops between
  services — Actuator/Prometheus metrics are already wired per-service, but
  request-level tracing isn't yet.
- Helm charts wrapping the raw manifests in `infra/k8s/` for templated,
  multi-environment (staging/prod) deploys.
