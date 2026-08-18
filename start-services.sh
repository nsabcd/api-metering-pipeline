#!/usr/bin/env bash

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

DISABLE_JWT="false"
DISABLE_RATE_LIMIT="false"

# Parse CLI arguments
for arg in "$@"; do
  case $arg in
    --skip-jwt|--no-jwt)
      DISABLE_JWT="true"
      shift
      ;;
    --skip-rate-limit|--no-rate-limit)
      DISABLE_RATE_LIMIT="true"
      shift
      ;;
    --skip-all)
      DISABLE_JWT="true"
      DISABLE_RATE_LIMIT="true"
      shift
      ;;
  esac
done

echo -e "${BLUE}=== 1. Starting Infrastructure (TimescaleDB, Redpanda, Redis) ===${NC}"
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

echo -e "\n${BLUE}=== 3. Starting discovery-server ===${NC}"
java -jar discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar > logs/discovery-server.log 2>&1 &
wait_for_service "discovery-server" 8761

echo -e "\n${BLUE}=== 4. Starting config-server ===${NC}"
java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar > logs/config-server.log 2>&1 &
wait_for_service "config-server" 8888

echo -e "\n${BLUE}=== 5. Starting Core Microservices ===${NC}"
java -jar services/event-metering-service/target/event-metering-service-0.0.1-SNAPSHOT.jar > logs/event-metering-service.log 2>&1 &
java -jar -Dspring.profiles.active=dev services/aggregation-engine/target/aggregation-engine-0.0.1-SNAPSHOT.jar > logs/aggregation-engine.log 2>&1 &
java -jar services/invoice-generator/target/invoice-generator-0.0.1-SNAPSHOT.jar > logs/invoice-generator.log 2>&1 &

wait_for_service "event-metering-service" 8081
wait_for_service "aggregation-engine" 8082
wait_for_service "invoice-generator" 8083

echo -e "\n${BLUE}=== 6. Starting api-gateway ===${NC}"

GATEWAY_JVM_ARGS=""
if [ "$DISABLE_JWT" = "true" ]; then
  echo -e "${GREEN}JWT Authentication disabled.${NC}"
  GATEWAY_JVM_ARGS="$GATEWAY_JVM_ARGS -Djwt.enabled=false"
fi

if [ "$DISABLE_RATE_LIMIT" = "true" ]; then
  echo -e "${GREEN}Rate Limiting disabled.${NC}"
  GATEWAY_JVM_ARGS="$GATEWAY_JVM_ARGS -Drate-limiter.enabled=false"
fi

java $GATEWAY_JVM_ARGS -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar > logs/api-gateway.log 2>&1 &
wait_for_service "api-gateway" 8080

echo -e "\n${GREEN}=== All services started successfully! ===${NC}"