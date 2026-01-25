#!/bin/bash

# Pet Care System - Demo Startup Script
# This script starts the entire application stack for demo purposes
# Supports both DEV (H2) and QAS (MSSQL) environments

set -e  # Exit on any error

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Default environment
ENV=${1:-qas}
ENV_UPPER=$(echo "$ENV" | tr '[:lower:]' '[:upper:]')

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Pet Care System - Starting Demo${NC}"
echo -e "${BLUE}  Environment: ${CYAN}${ENV_UPPER}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Validate environment
if [[ "$ENV" != "dev" && "$ENV" != "qas" ]]; then
    echo -e "${RED}Error: Invalid environment '${ENV}'${NC}"
    echo -e "${YELLOW}Usage: $0 [dev|qas]${NC}"
    echo -e "${YELLOW}  dev - Development mode (H2 database, port 3000)${NC}"
    echo -e "${YELLOW}  qas - QAS mode (MSSQL database, port 80)${NC}"
    exit 1
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi

# Set compose file and env file based on environment
COMPOSE_FILE="docker-compose.${ENV}.yml"
ENV_FILE=".env.${ENV}"

echo -e "${CYAN}Using configuration:${NC}"
echo -e "  Compose file: ${COMPOSE_FILE}"
echo -e "  Env file:     ${ENV_FILE}"
echo ""

# Load environment variables
if [ -f "$ENV_FILE" ]; then
    export $(cat "$ENV_FILE" | grep -v '^#' | xargs)
else
    echo -e "${RED}Error: Environment file ${ENV_FILE} not found${NC}"
    exit 1
fi

# Step 1: Start services based on environment
if [ "$ENV" = "qas" ]; then
    echo -e "${GREEN}[1/4] Starting Database (MSSQL)...${NC}"
    docker-compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d mssql

    echo -e "${YELLOW}⏳ Initializing database (this may take 30-45 seconds)...${NC}"
    sleep 15

    # Check database health (silent check)
    RETRY_COUNT=0
    MAX_RETRIES=30
    while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
        if docker-compose -f "$COMPOSE_FILE" exec -T mssql /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "${MSSQL_SA_PASSWORD}" -Q "SELECT 1" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ Database is ready${NC}"
            break
        fi
        RETRY_COUNT=$((RETRY_COUNT + 1))
        sleep 1
    done

    if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
        echo -e "${RED}✗ Database failed to start${NC}"
        echo -e "${YELLOW}Tip: Check logs with: docker-compose -f ${COMPOSE_FILE} logs mssql${NC}"
        exit 1
    fi

    STEP_BACKEND="[2/4]"
    STEP_FRONTEND="[3/4]"
    STEP_ANDROID="[4/4]"
else
    echo -e "${CYAN}DEV mode: Using H2 in-memory database (no MSSQL needed)${NC}"
    echo ""
    STEP_BACKEND="[1/3]"
    STEP_FRONTEND="[2/3]"
    STEP_ANDROID="[3/3]"
fi

echo ""
echo -e "${GREEN}${STEP_BACKEND} Starting Backend API (Spring Boot)...${NC}"
docker-compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d backend

echo -e "${YELLOW}⏳ Starting backend services (this may take 20-30 seconds)...${NC}"
sleep 10

# Check backend health (silent check with spinner)
RETRY_COUNT=0
MAX_RETRIES=40
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s http://localhost:${SERVER_PORT:-8080}/api/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Backend API is ready${NC}"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    sleep 1
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo -e "${YELLOW}⚠ Backend API health check timeout${NC}"
    echo -e "${YELLOW}Tip: Check logs with: docker-compose -f ${COMPOSE_FILE} logs backend${NC}"
fi

echo ""
echo -e "${GREEN}${STEP_FRONTEND} Starting Web Frontend...${NC}"
docker-compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d frontend

sleep 2

# Check frontend
FRONTEND_PORT_TO_CHECK=${FRONTEND_PORT:-80}
if [ "$ENV" = "dev" ]; then
    FRONTEND_PORT_TO_CHECK=3000
fi

if curl -s http://localhost:${FRONTEND_PORT_TO_CHECK} > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Web Frontend is ready${NC}"
else
    echo -e "${YELLOW}⚠ Frontend may need a few more seconds${NC}"
fi

echo ""
echo -e "${GREEN}${STEP_ANDROID} Android App Setup...${NC}"

# Check if Android emulator or device is connected
ADB_DEVICES=$(adb devices 2>/dev/null | grep -v "List" | grep "device" | wc -l || echo "0")
if [ $ADB_DEVICES -eq 0 ]; then
    echo -e "${YELLOW}No Android device detected (optional)${NC}"
    echo -e "${YELLOW}To install manually: cd android-app && ./gradlew installDebug${NC}"
else
    echo -e "${GREEN}Android device/emulator detected${NC}"
    read -p "Build and install Android app? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cd android-app
        echo -e "${GREEN}Building Android app...${NC}"
        ./gradlew clean assembleDebug installDebug

        echo -e "${GREEN}Launching app...${NC}"
        adb shell am start -n com.pet.petcare/.MainActivity
        cd ..
    fi
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}  ✓ Pet Care System is Ready!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}Environment: ${CYAN}${ENV_UPPER}${NC}"
echo ""
echo -e "${GREEN}Services:${NC}"

if [ "$ENV" = "qas" ]; then
    echo -e "  ${BLUE}•${NC} Database (MSSQL):  ${CYAN}localhost:1433${NC}"
fi

echo -e "  ${BLUE}•${NC} Backend API:       ${CYAN}http://localhost:${SERVER_PORT:-8080}${NC}"
echo -e "  ${BLUE}•${NC} Health Check:      ${CYAN}http://localhost:${SERVER_PORT:-8080}/api/health${NC}"

if [ "$ENV" = "dev" ]; then
    echo -e "  ${BLUE}•${NC} Web Frontend:      ${CYAN}http://localhost:3000${NC}"
else
    echo -e "  ${BLUE}•${NC} Web Frontend:      ${CYAN}http://localhost${NC}"
fi

echo ""
echo -e "${YELLOW}Test Accounts:${NC}"
echo -e "  Customer: ${CYAN}user01${NC} / ${CYAN}password123${NC}"
echo -e "  Sitter:   ${CYAN}sitter01${NC} / ${CYAN}sitter123${NC}"
echo -e "  Admin:    ${CYAN}admin${NC} / ${CYAN}admin123${NC}"
echo ""
echo -e "${YELLOW}Useful Commands:${NC}"
echo -e "  View logs:     ${CYAN}docker-compose -f ${COMPOSE_FILE} logs -f${NC}"
echo -e "  Stop all:      ${CYAN}./stop.sh ${ENV}${NC}"
echo -e "  Restart API:   ${CYAN}docker-compose -f ${COMPOSE_FILE} restart backend${NC}"
echo ""
