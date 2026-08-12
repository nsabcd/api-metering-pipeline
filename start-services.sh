#!/usr/bin/env bash

# Exit immediately if a command exits with a non-zero status
set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== 1. Starting Infrastructure (TimescaleDB & Redpanda) ===${NC}"
docker-compose up -d

echo -e "\n${BLUE}=== 2. Building Maven Modules ===${NC}"
mvn clean package -DskipTests

# Helper function to wait for a port to be ready
wait_for_service() {
  local service_name=$1
  local port=$2
  echo -n "Waiting for $service_name to start on port $port..."
  while ! nc -z localhost $port; do
    sleep 2
    echo -n "."
  done
  echo -e " ${GREEN}[READY]${NC}"
}

echo -e "\n${BLUE}=== 3. Starting discovery-server (Eureka) ===${NC}"
java -jar discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar > logs/discovery-server.log 2>&1 &
wait_for_service "discovery-server" 8761

echo -e "\n${BLUE}=== 4. Starting config-server ===${NC}"
java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar > logs/config-server.log 2>&1 &
wait_for_service "config-server" 8888

echo -e "\n${BLUE}=== 5. Starting Core Microservices ===${NC}"

echo "Starting event-metering-service..."
java -jar services/event-metering-service/target/event-metering-service-0.0.1-SNAPSHOT.jar > logs/event-metering-service.log 2>&1 &

echo "Starting aggregation-engine..."
java -jar services/aggregation-engine/target/aggregation-engine-0.0.1-SNAPSHOT.jar > logs/aggregation-engine.log 2>&1 &

echo "Starting invoice-generator..."
java -jar services/invoice-generator/target/invoice-generator-0.0.1-SNAPSHOT.jar > logs/invoice-generator.log 2>&1 &

# Wait for core microservices ports
wait_for_service "event-metering-service" 8081
wait_for_service "aggregation-engine" 8082
wait_for_service "invoice-generator" 8083

echo -e "\n${BLUE}=== 6. Starting api-gateway ===${NC}"
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar > logs/api-gateway.log 2>&1 &
wait_for_service "api-gateway" 8080

echo -e "\n${GREEN}=== All services started successfully! ===${NC}"