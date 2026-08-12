#!/usr/bin/env bash

RED='\033[0;31m'
BLUE='\033[0;34m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== 1. Stopping Spring Boot Java Applications ===${NC}"

# Find and kill processes running the service jars
SERVICES=(
  "api-gateway"
  "event-metering-service"
  "aggregation-engine"
  "invoice-generator"
  "config-server"
  "discovery-server"
)

for service in "${SERVICES[@]}"; do
  # Find PID based on JAR name
  PID=$(pgrep -f "${service}-0.0.1-SNAPSHOT.jar" || true)

  if [ -n "$PID" ]; then
    echo -e "Stopping ${service} (PID: ${PID})..."
    kill -15 "$PID" # Graceful shutdown signal
  else
    echo "No running instance found for ${service}."
  fi
done

# Short pause to let Java processes shut down cleanly
sleep 3

# Force kill any lingering processes if they didn't exit gracefully
for service in "${SERVICES[@]}"; do
  PID=$(pgrep -f "${service}-0.0.1-SNAPSHOT.jar" || true)
  if [ -n "$PID" ]; then
    echo -e "${RED}Force killing persistent process ${service} (PID: ${PID})...${NC}"
    kill -9 "$PID"
  fi
done

echo -e "\n${BLUE}=== 2. Stopping Docker Containers (TimescaleDB & Redpanda) ===${NC}"
docker-compose down

echo -e "\n${GREEN}=== All services stopped successfully! ===${NC}"