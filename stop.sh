#!/bin/bash

# 🛑 Portfolio Full Stack Stop Script
# Stops all running services

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}🛑 Stopping Portfolio services...${NC}"

# Kill processes on ports
echo -e "${YELLOW}   Stopping Backend (port 8080)...${NC}"
lsof -ti:8080 | xargs kill -9 2>/dev/null && echo -e "${GREEN}   ✅ Backend stopped${NC}" || echo -e "${RED}   ℹ️  No process on port 8080${NC}"

echo -e "${YELLOW}   Stopping Frontend (port 4200)...${NC}"
lsof -ti:4200 | xargs kill -9 2>/dev/null && echo -e "${GREEN}   ✅ Frontend stopped${NC}" || echo -e "${RED}   ℹ️  No process on port 4200${NC}"

# Kill any mvnw or ng serve processes
pkill -f "mvnw" 2>/dev/null
pkill -f "ng serve" 2>/dev/null

echo -e "${GREEN}✅ All services stopped${NC}"
