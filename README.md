# 🚀 API Usage Metering & Billing Pipeline for SaaS Applications

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue.svg?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-cloud)
[![Redpanda](https://img.shields.io/badge/Redpanda-v23.3.10-red.svg?style=for-the-badge&logo=redpanda)](https://redpanda.com/)
[![TimescaleDB](https://img.shields.io/badge/TimescaleDB-PG15-blue.svg?style=for-the-badge&logo=postgresql)](https://www.timescale.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

A distributed, enterprise-ready microservices architecture designed for high-throughput, low-latency API usage metering, time-series metrics aggregation, and automated usage-based invoicing (similar to Togai, Metering.ai, or Stripe Invoicing).

---

## 📑 Table of Contents
1. [Architecture Overview](#-architecture-overview)
2. [Microservice Ecosystem & Tech Stack](#-microservice-ecosystem--tech-stack)
3. [Key Architectural Highlights & Patterns](#-key-architectural-highlights--patterns)
4. [Directory & Project Layout](#-directory--project-layout)
5. [Prerequisites & System Requirements](#-prerequisites--system-requirements)
6. [Step-by-Step Local Development Setup](#-step-by-step-local-development-setup)
7. [Comprehensive Testing & Verification Guide](#-comprehensive-testing--verification-guide)
8. [Database Schema & Migrations](#-database-schema--migrations)
9. [Configuration & Environment Management](#-configuration--environment-management)
10. [Troubleshooting & FAQs](#-troubleshooting--faqs)
11. [License](#-license)

---

## 🏗️ Architecture Overview

Modern AI and cloud SaaS products require granular metering of non-uniform consumption metrics (e.g., LLM prompt/completion tokens, database read/write units, vector queries, or serverless execution time).

This pipeline ingests streaming usage pings from edge gateways, safely stores them in event brokers with zero data loss, processes time-bucketed aggregations in a specialized time-series engine, and generates accurate periodic billing invoices.

```
                                 [ API Gateway ] (Port: 8080)
                                        |
                 +----------------------+----------------------+
                 | (Route: /api/v1/events/**)                  | (Route: /api/v1/invoices/**)
                 v                                             v
    +-------------------------+                   +-------------------------+
    | Event Metering Service  |                   |    Invoice Generator    |
    |       (Port: 8081)      |                   |       (Port: 8083)      |
    +-------------------------+                   +-------------------------+
                 |                                             |
                 | (Kafka Producer)                            | (Quartz Job Scheduler / 
                 v                                             |  Spring WebClient REST)
    +-------------------------+                                |
    |   Redpanda Stream Bus   |                                |
    |  Topic: api-usage-events|                                |
    |       (Port: 9092)      |                                |
    +-------------------------+                                |
                 |                                             |
                 | (Kafka Consumer)                            |
                 v                                             |
    +-------------------------+                                |
    |   Aggregation Engine    | <------------------------------+
    |       (Port: 8082)      | (Fetches aggregated summaries)
    +-------------------------+
                 |
                 | (Spring Data JPA / Flyway)
                 v
    +--------------------------------------------------+
    |  TimescaleDB (PostgreSQL 15 Time-Series DB)      |
    |  - api_usage_events (Hypertable / Raw Logs)      |
    |  - hourly_customer_usage (Rollup Aggregations)   |
    |  - invoices & QRTZ_* (Billing & Job Locks)       |
    |                   (Port: 5432)                   |
    +--------------------------------------------------+
```

---

## 📦 Microservice Ecosystem & Tech Stack

### Service Matrix

| Service / Infrastructure | Port | Tech Stack | Responsibility |
| :--- | :--- | :--- | :--- |
| **`discovery-server`** | `8761` | Spring Cloud Netflix Eureka | Dynamic service registry and heartbeats for client load-balancing. |
| **`config-server`** | `8888` | Spring Cloud Config (`native`) | Centralized runtime configuration server reading classpath-based YAML settings. |
| **`api-gateway`** | `8080` | Spring Cloud Gateway, Virtual Threads | Unified reverse proxy routing traffic, performing load balancing (`lb://`), and security isolation. |
| **`event-metering-service`** | `8081` | Spring Boot 3.2, Kafka Producer, Jackson JSR-310 | High-frequency endpoint ingesting telemetry pings, auto-enriching UUIDs/timestamps, and streaming to Redpanda. |
| **`aggregation-engine`** | `8082` | Spring Boot 3.2, Spring Kafka, Flyway, TimescaleDB | Event stream listener with idempotent DB ingestion (`ON CONFLICT DO NOTHING`) and REST summary reporting. |
| **`invoice-generator`** | `8083` | Spring Boot 3.2, Quartz Scheduler, Spring WebClient | Automated billing cycle job executor; computes token totals, applies pricing rules, and stores persistent invoices. |
| **`Redpanda`** | `9092` / `9644` | Redpanda v23.3.10 | High-throughput, C++-based Kafka API-compatible event bus with lower latency and memory footprint than Zookeeper/Kafka. |
| **`TimescaleDB`** | `5432` | PostgreSQL 15 + TimescaleDB Extension | Optimized relational time-series database engineered for high write rates and time-bucket aggregations. |

---

## ⚡ Key Architectural Highlights & Patterns

1. **Java 21 Virtual Threads (Project Loom)**: Enabled across all Spring Boot microservices (`spring.threads.virtual.enabled=true`) for non-blocking execution under heavy concurrency.
2. **Idempotent Ingestion & Deduplication**: Custom SQL native queries with composite key constraints `(event_id, timestamp)` prevent double-counting under network retry conditions (`saveIdempotent`).
3. **Dead Letter Topic (DLT) Fault Tolerance**: Malformed event payloads or unresolvable deserialization errors are safely forwarded to `api-usage-events.DLT` with exponential backoff retries.
4. **Clustered Quartz Job Scheduling**: Database-backed Quartz lock tables (`QRTZ_*`) in PostgreSQL ensure horizontal scaling safety without race conditions across multiple instances of `invoice-generator`.
5. **Declarative Http Interfaces**: Declarative WebClient proxy interfaces (`AggregationEngineClient`) provide clean inter-service RPC communication via Eureka service discovery.

---

## 📂 Directory & Project Layout

```
api-metering-pipeline/
├── .idea/                             # Development workspace configuration
├── api-gateway/                       # Reverse proxy entry point
│   ├── src/main/java/com/metering/gateway/
│   └── src/main/resources/application.yml
├── config-server/                     # Centralized configuration manager
│   ├── src/main/java/com/metering/configserver/
│   └── src/main/resources/application.yml
├── discovery-server/                  # Eureka service discovery node
│   ├── src/main/java/com/metering/discoveryserver/
│   └── src/main/resources/application.yml
├── services/
│   ├── event-metering-service/       # Telemetry ingestion endpoint
│   │   ├── src/main/java/com/metering/events/
│   │   └── src/main/resources/application.yml
│   ├── aggregation-engine/           # Event consumer & time-series aggregation service
│   │   ├── src/main/java/com/metering/aggregation/
│   │   └── src/main/resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── db/migration/         # Flyway schema migrations
│   └── invoice-generator/            # Quartz-scheduled billing service
│       ├── src/main/java/com/metering/invoice/
│       └── src/main/resources/application.yml
├── docker-compose.yml                 # TimescaleDB & Redpanda container orchestrator
├── start-services.sh                  # Automation script for orchestration
├── stop-services.sh                   # Process teardown and container cleanup script
├── pom.xml                            # Parent multi-module Maven POM
└── README.md                          # Repository documentation
```

---

## 🛠️ Prerequisites & System Requirements

Before setting up the project locally, verify that your development machine has the following installed:

- **JDK 21** or higher (`java -version`)
- **Apache Maven 3.8+** (`mvn -version`)
- **Docker Engine 20.10+ & Docker Compose v2.0+** (`docker compose version`)
- **cURL** or Postman (for API testing)
- **netcat (`nc`)**: Used by bash health check scripts (`nc -z localhost <port>`)

---

## 🚀 Step-by-Step Local Development Setup

### Option 1: Automated Turnkey Startup (Recommended)

The workspace includes shell automation scripts to spin up dependencies, compile code, start infrastructure, and verify service readiness.

```bash
# 1. Clone repository
git clone https://github.com/your-org/api-metering-pipeline.git
cd api-metering-pipeline

# 2. Make startup scripts executable
chmod +x start-services.sh stop-services.sh

# 3. Launch full ecosystem
./start-services.sh

## use ./start-services.sh --skip-all for skipping rate limiting and JWT auth check
## use ./start-services.sh --skip-rate-limit for skipping rate limiting 
## use ./start-services.sh --skip-jwt for skipping and JWT auth check

## user ./stop-services.sh to kill all containers and JVMs

```

#### What `start-services.sh` does automatically:
1. Spawns `TimescaleDB` and `Redpanda` in Docker.
2. Compiles and packages all Maven modules (`mvn clean package -DskipTests`).
3. Boots `discovery-server` (Port 8761) and blocks until health port is open.
4. Boots `config-server` (Port 8888) and verifies readiness.
5. Boots core domain microservices (`event-metering-service`, `aggregation-engine`, `invoice-generator`) in background mode (`nohup` / log redirection).
6. Boots `api-gateway` (Port 8080) and prints system ready banner.

---

### Option 2: Manual Step-by-Step Setup

If you prefer running services individually inside an IDE (e.g. IntelliJ IDEA, Eclipse) or terminal windows:

#### Step 1: Start Infrastructure Containers
```bash
docker-compose up -d
```
*Verify container status:* `docker-compose ps` (Ensure `metering-timescaledb` and `metering-redpanda` are healthy).

#### Step 2: Build the Parent Maven Project
```bash
mvn clean install -DskipTests
```

#### Step 3: Launch Services in Order
Run each application class in sequence (allow 10-15 seconds between launches for registration):

1. **Discovery Server**: `com.metering.discoveryserver.DiscoveryServerApplication` (`:8761`)
2. **Config Server**: `com.metering.configserver.ConfigServerApplication` (`:8888`)
3. **Event Metering**: `com.metering.events.EventMeteringApplication` (`:8081`)
4. **Aggregation Engine**: `com.metering.aggregation.AggregationEngineApplication` with active profile `dev` (`:8082`)
5. **Invoice Generator**: `com.metering.invoice.InvoiceGeneratorApplication` (`:8083`)
6. **API Gateway**: `com.metering.gateway.ApiGatewayApplication` (`:8080`)

---

## 🧪 Comprehensive Testing & Verification Guide

Follow this sequence to test end-to-end functionality, stream ingestion, event consumption, database persistence, and invoice generation.

### 1. Verify Service Discovery
Open your browser and navigate to the Eureka Dashboard:
[http://localhost:8761](http://localhost:8761)

**Expected Output:** You should see active registrations for:
- `API-GATEWAY`
- `EVENT-METERING-SERVICE`
- `AGGREGATION-ENGINE`
- `INVOICE-GENERATOR`

---

### 2. Test Ingestion via API Gateway
Send multiple usage events to the API Gateway endpoint (`http://localhost:8080/api/v1/events`).

#### Single Event Test (LLM Completion):
```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "cust_acme_corp",
    "apiEndpoint": "/v1/chat/completions",
    "responseTimeMs": 340,
    "tokensUsed": 2500,
    "statusCode": 200
  }'
```
*Expected Response:* `HTTP/1.1 202 Accepted`

#### Simulate Batch Traffic (Shell Loop):
Run a loop to emit events for multiple customers:
```bash
for i in {1..10}; do
  curl -s -X POST http://localhost:8080/api/v1/events \
    -H "Content-Type: application/json" \
    -d "{
      "customerId": "cust_acme_corp",
      "apiEndpoint": "/v1/embeddings",
      "responseTimeMs": $((RANDOM % 200 + 50)),
      "tokensUsed": 500,
      "statusCode": 200
    }" > /dev.null
done

for i in {1..5}; do
  curl -s -X POST http://localhost:8080/api/v1/events \
    -H "Content-Type: application/json" \
    -d "{
      "customerId": "cust_globex",
      "apiEndpoint": "/v1/chat/completions",
      "responseTimeMs": $((RANDOM % 500 + 100)),
      "tokensUsed": 12000,
      "statusCode": 200
    }" > /dev.null
done
echo "Batch event emission complete!"
```

---

### 3. Verify Kafka Stream Processing Logs
Inspect the live aggregation service logs to confirm Redpanda topic consumption:

```bash
tail -f logs/aggregation-engine.log
```
*Expected Log Entry:*
```text
INFO  [aggregation-engine] com.metering.aggregation.listener.UsageEventListener : Received event for Customer: [cust_acme_corp] | Endpoint: [/v1/chat/completions] | Tokens: [2500]
```

---

### 4. Query Aggregated Usage Summaries
Retrieve aggregated customer consumption metrics over a specified date/time window directly from the Aggregation Engine (`http://localhost:8082`):

```bash
curl -X GET "http://localhost:8082/api/v1/usage/summary?start=2026-08-01T00:00:00Z&end=2026-08-30T23:59:59Z"
```

*Expected JSON Output:*
```json
[
  {
    "customerId": "cust_acme_corp",
    "totalTokens": 7500,
    "totalRequests": 11
  },
  {
    "customerId": "cust_globex",
    "totalTokens": 60000,
    "totalRequests": 5
  }
]
```

---

### 5. Test Invoice Generation Trigger
The `invoice-generator` runs every 5 minutes by default via Quartz (`0 0/5 * * * ?`). To test it on-demand without waiting for the cron trigger, call the test endpoint:

```bash
curl -X POST "http://localhost:8083/api/v1/invoices/test-fetch-usage"
```

Check the `invoice-generator` logs:
```bash
tail -f logs/invoice-generator.log
```

*Expected Log Entries:*
```text
INFO [invoice-generator] com.metering.invoice.job.MonthlyInvoiceJob : Starting monthly invoice generation job...
INFO [invoice-generator] com.metering.invoice.job.MonthlyInvoiceJob : Successfully generated Invoice [INV-A8F2E1B4] for Customer [cust_acme_corp] | Tokens: 7500 | Amount: $0.02
INFO [invoice-generator] com.metering.invoice.job.MonthlyInvoiceJob : Successfully generated Invoice [INV-C9B0E3D1] for Customer [cust_globex] | Tokens: 60000 | Amount: $0.12
```

---

### 6. Verify Database Tables (TimescaleDB)
Connect directly to the PostgreSQL database to verify raw events and generated invoices:

```bash
docker exec -it metering-timescaledb psql -U postgres -d metering_db
```

Run interactive SQL queries:
```sql
-- Query ingested raw event records
SELECT event_id, customer_id, api_endpoint, tokens_used, timestamp 
FROM api_usage_events 
ORDER BY timestamp DESC 
LIMIT 5;

-- Query generated customer invoices
SELECT invoice_id, customer_id, total_tokens, total_amount, status, period_start, period_end 
FROM invoices;

-- Exit psql terminal
\q
```

---

## 🗄️ Database Schema & Migrations

Database structures are managed automatically by **Flyway** in the `aggregation-engine` service and Spring JPA auto-init in `invoice-generator`.

### Key Tables Overview:

1. **`api_usage_events`**: Raw event store partitioned as a hypertable in TimescaleDB.
    - Primary Key: `(event_id, timestamp)`
    - Stores single usage events with response times, status codes, and token counts.
2. **`hourly_customer_usage`**: Rollup view / aggregated metrics table.
    - Primary Key: `(bucket, customer_id, api_endpoint)`
3. **`invoices`**: Billing ledger storing persistent billing period statements.
    - Stores `invoice_id`, `customer_id`, `total_amount`, `total_tokens`, `status` (`PENDING`, `PAID`, `FAILED`), and `period_start`/`period_end`.
4. **`QRTZ_*`**: 11 standard Quartz scheduler cluster management tables tracking active triggers, job details, and row locks.

---

## ⚙️ Configuration & Environment Management

Service configuration is powered by Spring Cloud Config Server reading YAML definitions from `config-server/src/main/resources/config`.

### Key Config Variables

| Variable / Parameter | Default Value | Target Service | Description |
| :--- | :--- | :--- | :--- |
| `server.port` | `8080`, `8081`, `8082`, `8083`, `8761`, `8888` | All Services | Dedicated HTTP port for each service module. |
| `spring.kafka.bootstrap-servers` | `localhost:9092` | Metering, Aggregation | Redpanda / Kafka broker list connection string. |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/metering_db` | Aggregation, Invoice | TimescaleDB connection URL. |
| `spring.quartz.properties.org.quartz.jobStore.isClustered` | `true` | Invoice Generator | Enables multi-node Quartz lock management in DB. |
| `PRICE_PER_TOKEN` | `0.000002` ($2 / 1M Tokens) | Invoice Generator | Base billing rate applied during monthly invoicing. |

---

## 🛑 Stopping & Cleaning Up

To gracefully terminate all running Java processes and shut down Docker infrastructure:

```bash
./stop-services.sh
```

To purge all persistent data volumes (e.g. start with a fresh database):
```bash
docker-compose down -v
```

---

## 🔍 Troubleshooting & FAQs

### Q1: Connection Refused on `localhost:9092` (Redpanda)
* **Cause**: Docker container `metering-redpanda` is still initializing or failed to start.
* **Fix**: Run `docker-compose logs redpanda` to inspect status. Ensure port `9092` is not occupied by a local Kafka instance.

### Q2: Flyway Migration Errors on Aggregation Engine
* **Cause**: Database schema altered manually outside Flyway.
* **Fix**: Reset the development database using `docker-compose down -v && docker-compose up -d`.

### Q3: Eureka Services Not Showing Up in Gateway
* **Cause**: Service registration delay during startup.
* **Fix**: Wait 30 seconds for heartbeats to sync. Verify `eureka.client.serviceUrl.defaultZone` points to `http://localhost:8761/eureka/`.

---

## 📄 License

Distributed under the **MIT License**. See `LICENSE` for more information.
