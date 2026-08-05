# Architecture

This mirrors the original whiteboard diagram exactly, service for service.

```
Driver Mobile App
      │  GPS / speed / telemetry (every ~3s)
      ▼
API Gateway / LB  ──────────────────────────────────────┐
      │  publishes to Kafka                             │ REST (JWT-authenticated)
      ▼                                                  │
Event Streaming (Kafka: vehicle.telemetry)               │
      │                              │                   │
      ▼                              ▼                   │
Monitoring Service              Route Optimization        │
  + AI Agent Layer                 Service                │
      │                              │                    │
      │   both call the AI Agent Layer over REST          │
      │        (Monitoring Agent / Route Agent)            │
      ▼                              ▼                    │
   driver.alerts (Kafka)        route.updates (Kafka)      │
      └──────────────┬───────────────┘                    │
                      ▼                                    │
           Notification & Alert Service ◄───────────────────┘
           (Alert Agent + Approval Agent, STOMP/WebSocket)
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
Supervisor Dashboard          Driver Mobile App
(+ Conversation Agent chat)

PostgreSQL (+pgvector) / Redis / S3(MinIO) underpin every service above.
```

## Why a separate ai-agent-service instead of embedding agents in each service

The original diagram draws "Monitoring Service & AI agent Layer" as one box, but
the legend at the bottom lists five agents that are each relevant to different
services — Route Agent to route-optimization-service, Alert/Approval Agent to
notification-alert-service, Conversation Agent to the dashboard. Centralizing
them in ai-agent-service means:

- One place to version prompts, swap/upgrade the LLM, and evaluate agent
  accuracy — matching the "LLM Evaluation & Optimization" and "Prompt
  Engineering" line items in the role this platform is built to demonstrate.
- Other services stay thin: they assemble context and call a REST endpoint,
  rather than each maintaining its own LLM client, retry/circuit-breaker logic,
  and prompt versions.
- The RAG pipeline (pgvector) and its ingestion path live in one service instead
  of being duplicated wherever a chat feature is needed.

## Event contracts

| Topic                   | Producer                    | Consumers                                              |
|--------------------------|------------------------------|---------------------------------------------------------|
| `vehicle.telemetry`      | api-gateway                 | monitoring-service, route-optimization-service (fan-out, separate consumer groups) |
| `driver.alerts`          | monitoring-service           | notification-alert-service                              |
| `route.updates`          | route-optimization-service   | notification-alert-service                              |
| `approval.decisions`     | notification-alert-service (reserved for async approval audit trail) | — |
| `notification.dispatched`| notification-alert-service (reserved for delivery audit trail) | — |

## Two-tier detection (why not just "call the LLM on everything")

`monitoring-service` runs cheap deterministic rule checks (speed threshold,
harsh braking/acceleration flags, engine temperature) on **every** telemetry
sample. Only samples that trip a rule get escalated to the Monitoring Agent for
pattern-level judgment across the recent window. This keeps LLM call volume
proportional to actual anomalies, not proportional to telemetry volume — the
same triage-then-escalate shape as the "Automated Root Cause Analysis Engine"
project in the source resume.

## Human-in-the-loop

`requiresApproval` is a first-class field on agent responses, not an
afterthought. The Approval Agent only self-approves clearly bounded, low-risk
actions; anything ambiguous is escalated and shows up in the dashboard's
Alerts & Approvals panel, where a supervisor approves or rejects it explicitly.
Wrong auto-approvals are expensive; a slightly delayed decision usually isn't —
the agent is deliberately biased toward escalation.

## What's intentionally out of scope here

- **Real GPS/traffic data** — the driver app simulates telemetry and
  route-optimization-service uses a placeholder candidate-route generator.
  Swap in a real routing/traffic API (Google Directions, HERE) and real device
  sensors to go from demo to production.
- **Multi-broker Kafka / HA Postgres** — the k8s manifests run single
  instances of each with notes pointing at the Strimzi Kafka Operator and a
  managed/operator-backed Postgres for real HA.
- **OAuth2/OIDC** — the gateway's JWT filter is a shared-secret HMAC validator
  suitable for a scaffold; swap for token introspection against the client's
  real identity provider.
