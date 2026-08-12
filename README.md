# ⚡ API Usage Metering & Billing Pipeline

An event-driven, microservices-based platform for metering high-frequency API usage (tokens, DB calls, compute minutes) and generating automated tier-based invoices in SaaS applications.

---

## 🏗️ High-Level Architecture

```text
                             [ API Clients ]
                                    │
                                    ▼
                          ┌──────────────────┐
                          │   API Gateway    │ (Spring Cloud Gateway)
                          └────────┬─────────┘
                                   │
                                   │ High-throughput HTTP pings
                                   ▼
                      ┌──────────────────────────┐
                      │  Event Metering Service  │
                      └────────────┬─────────────┘
                                   │
                                   │ Publish event (`usage-events` topic)
                                   ▼
                        ┌──────────────────────┐
                        │   Redpanda / Kafka   │ (Streaming Log)
                        └──────────┬───────────┘
                                   │
                                   │ Stream consumption
                                   ▼
                       ┌────────────────────────┐
                       │   Aggregation Engine   │
                       └───────────┬────────────┘
                                   │
                                   │ Hypertable Insert / Time-bucket Continuous Aggr.
                                   ▼
                        ┌──────────────────────┐
                        │     TimescaleDB      │ (Time-Series Database)
                        └──────────┬───────────┘
                                   │
                                   │ Cron / Quartz Scheduler Query
                                   ▼
                        ┌──────────────────────┐
                        │  Invoice Generator   │
                        └──────────────────────┘