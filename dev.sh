#!/bin/bash

# 🚀 Portfolio Full Stack Development Script
# This script starts both Backend (Spring Boot) and Frontend (Angular)

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Banner
echo -e "${CYAN}"
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                                                            ║"
echo "║        🚀 Portfolio Full Stack Development Server        ║"
echo "║                                                            ║"
echo "║  Backend:  Spring Boot (Port 8080)                        ║"
echo "║  Frontend: Angular 20  (Port 4200)                        ║"
echo "║                                                            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Function to kill processes on specific ports
cleanup() {
    echo -e "${YELLOW}🧹 Cleaning up ports...${NC}"
    lsof -ti:8080 | xargs kill -9 2>/dev/null || true
    lsof -ti:4200 | xargs kill -9 2>/dev/null || true
    echo -e "${GREEN}✅ Ports cleaned${NC}"
}

# Function to check if port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${RED}❌ Port $port is already in use${NC}"
        echo -e "${YELLOW}   Run './dev.sh --clean' to kill processes${NC}"
        return 1
    fi
    return 0
}

# Function to check if PostgreSQL is running
check_postgres() {
    echo -e "${BLUE}🐘 Checking PostgreSQL...${NC}"
    if ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
        echo -e "${RED}❌ PostgreSQL is not running${NC}"
        echo -e "${YELLOW}   Start it with: brew services start postgresql${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ PostgreSQL is running${NC}"
}

# Function to check if database exists
check_database() {
    echo -e "${BLUE}🗄️  Checking database...${NC}"
    if ! psql -lqt | cut -d \| -f 1 | grep -qw portfolio_db; then
        echo -e "${YELLOW}⚠️  Database 'portfolio_db' not found${NC}"
        echo -e "${BLUE}   Creating database...${NC}"
        createdb portfolio_db
        echo -e "${GREEN}✅ Database created${NC}"
    else
        echo -e "${GREEN}✅ Database exists${NC}"
    fi
}

# Function to start backend
start_backend() {
    echo -e "${PURPLE}☕ Starting Spring Boot Backend...${NC}"
    cd "$(dirname "$0")"

    # Check if mvnw is executable
    if [[ ! -x ./mvnw ]]; then
        chmod +x ./mvnw
    fi

    # Start Spring Boot in background
    ./mvnw spring-boot:run &
    BACKEND_PID=$!

    echo -e "${GREEN}✅ Backend started (PID: $BACKEND_PID)${NC}"
    echo -e "${BLUE}   Logs: http://localhost:8080/actuator/health${NC}"
}

# Function to start frontend
start_frontend() {
    echo -e "${CYAN}⚡ Starting Angular Frontend...${NC}"
    cd "$(dirname "$0")/../portfolio-frontend"

    # Check if node_modules exists
    if [[ ! -d "node_modules" ]]; then
        echo -e "${YELLOW}⚠️  node_modules not found. Running npm install...${NC}"
        npm install
    fi

    # Start Angular dev server
    npm start &
    FRONTEND_PID=$!

    echo -e "${GREEN}✅ Frontend started (PID: $FRONTEND_PID)${NC}"
    echo -e "${BLUE}   URL: http://localhost:4200${NC}"
}

# Function to wait for services
wait_for_services() {
    echo ""
    echo -e "${YELLOW}⏳ Waiting for services to start...${NC}"
    echo ""

    # Wait for backend
    echo -e "${PURPLE}   Waiting for Backend (Spring Boot)...${NC}"
    for i in {1..60}; do
        if curl -s http://localhost:8080/api/health >/dev/null 2>&1; then
            echo -e "${GREEN}   ✅ Backend is ready!${NC}"
            break
        fi
        echo -ne "${YELLOW}   ⏳ Attempt $i/60...\r${NC}"
        sleep 2
    done

    # Wait for frontend
    echo -e "${CYAN}   Waiting for Frontend (Angular)...${NC}"
    for i in {1..60}; do
        if curl -s http://localhost:4200 >/dev/null 2>&1; then
            echo -e "${GREEN}   ✅ Frontend is ready!${NC}"
            break
        fi
        echo -ne "${YELLOW}   ⏳ Attempt $i/60...\r${NC}"
        sleep 2
    done
}

# Function to show final message
show_ready_message() {
    echo ""
    echo -e "${GREEN}"
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║                                                            ║"
    echo "║                    🎉 ALL SYSTEMS GO! 🎉                   ║"
    echo "║                                                            ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo -e "${CYAN}🌐 Frontend:${NC} http://localhost:4200"
    echo -e "${PURPLE}🔌 Backend:${NC}  http://localhost:8080"
    echo -e "${BLUE}❤️  Health:${NC}   http://localhost:8080/api/health"
    echo -e "${YELLOW}📚 API Docs:${NC} http://localhost:8080/swagger-ui.html (if enabled)"
    echo ""
    echo -e "${RED}Press Ctrl+C to stop all services${NC}"
    echo ""
}

# Trap Ctrl+C and cleanup
trap cleanup EXIT INT TERM

# Main script
case "${1:-}" in
    --clean)
        cleanup
        exit 0
        ;;
    --backend-only)
        check_postgres
        check_database
        check_port 8080 || exit 1
        start_backend
        wait
        ;;
    --frontend-only)
        check_port 4200 || exit 1
        start_frontend
        wait
        ;;
    --help)
        echo "Usage: ./dev.sh [options]"
        echo ""
        echo "Options:"
        echo "  (no args)         Start both backend and frontend"
        echo "  --clean           Kill processes on ports 4200 and 8080"
        echo "  --backend-only    Start only Spring Boot backend"
        echo "  --frontend-only   Start only Angular frontend"
        echo "  --help            Show this help message"
        exit 0
        ;;
    *)
        # Default: Start both
        check_postgres
        check_database
        check_port 8080 || exit 1
        check_port 4200 || exit 1

        start_backend
        sleep 5  # Give backend a head start
        start_frontend

        wait_for_services
        show_ready_message

        # Wait for both processes
        wait
        ;;
esac
