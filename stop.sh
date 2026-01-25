#!/bin/bash

# Pet Care System - Demo Shutdown Script
# This script stops all running services
# Supports both DEV and QAS environments

set -e

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
echo -e "${BLUE}  Pet Care System - Stopping Services${NC}"
echo -e "${BLUE}  Environment: ${CYAN}${ENV_UPPER}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Validate environment
if [[ "$ENV" != "dev" && "$ENV" != "qas" ]]; then
    echo -e "${RED}Error: Invalid environment '${ENV}'${NC}"
    echo -e "${YELLOW}Usage: $0 [dev|qas]${NC}"
    echo -e "${YELLOW}  dev - Stop development environment${NC}"
    echo -e "${YELLOW}  qas - Stop QAS environment${NC}"
    exit 1
fi

# Set compose file based on environment
COMPOSE_FILE="docker-compose.${ENV}.yml"
ENV_FILE=".env.${ENV}"

if [ ! -f "$COMPOSE_FILE" ]; then
    echo -e "${RED}Error: Compose file ${COMPOSE_FILE} not found${NC}"
    exit 1
fi

# Stop Docker containers
echo -e "${YELLOW}Stopping Docker containers...${NC}"
docker-compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down

echo ""
echo -e "${GREEN}✓ All services stopped${NC}"
echo ""
echo -e "${YELLOW}Note: Database data is preserved in Docker volume${NC}"
echo -e "${YELLOW}To completely remove all data, run:${NC}"
echo -e "${YELLOW}  docker-compose -f ${COMPOSE_FILE} down -v${NC}"
echo ""
echo -e "${YELLOW}To restart services:${NC}"
echo -e "${YELLOW}  ./start.sh ${ENV}${NC}"
echo ""
