#!/bin/bash

# Catalyst Infrastructure Management Script
# Bash script for managing Docker Compose services

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

function show_header() {
    echo -e "\n${CYAN}🚀 Catalyst Infrastructure Manager${NC}\n"
}

function show_help() {
    echo -e "${YELLOW}Usage: ./catalyst.sh [command]${NC}\n"
    echo -e "${GREEN}Commands:${NC}"
    echo -e "  ${WHITE}start${NC}     - Start all services"
    echo -e "  ${WHITE}stop${NC}      - Stop all services"
    echo -e "  ${WHITE}restart${NC}   - Restart all services"
    echo -e "  ${WHITE}status${NC}    - Show service status"
    echo -e "  ${WHITE}logs${NC}      - Show service logs (follow mode)"
    echo -e "  ${WHITE}clean${NC}     - Stop and remove all volumes (⚠️  deletes data)"
    echo -e "  ${WHITE}init${NC}      - Initialize environment file"
    echo -e "  ${WHITE}test${NC}      - Test service connectivity"
    echo -e "  ${WHITE}help${NC}      - Show this help message\n"
}

function init_environment() {
    echo -e "${YELLOW}📝 Initializing environment...${NC}"
    
    if [ -f ".env" ]; then
        echo -e "${YELLOW}⚠️  .env file already exists. Skipping...${NC}"
    else
        cp env.example .env
        echo -e "${GREEN}✅ Created .env file from env.example${NC}"
        echo -e "${CYAN}💡 Edit .env file to customize configuration${NC}\n"
    fi
}

function start_services() {
    echo -e "${YELLOW}🚀 Starting Catalyst services...${NC}"
    
    if [ ! -f ".env" ]; then
        echo -e "${YELLOW}⚠️  .env file not found. Creating from template...${NC}"
        init_environment
    fi
    
    docker-compose up -d
    
    if [ $? -eq 0 ]; then
        echo -e "\n${GREEN}✅ Services started successfully!${NC}"
        echo -e "${CYAN}💡 Run './catalyst.sh status' to check service health${NC}\n"
        show_service_info
    else
        echo -e "${RED}❌ Failed to start services${NC}"
        exit 1
    fi
}

function stop_services() {
    echo -e "${YELLOW}🛑 Stopping Catalyst services...${NC}"
    docker-compose down
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Services stopped successfully!${NC}\n"
    else
        echo -e "${RED}❌ Failed to stop services${NC}"
        exit 1
    fi
}

function restart_services() {
    echo -e "${YELLOW}🔄 Restarting Catalyst services...${NC}"
    stop_services
    sleep 2
    start_services
}

function show_status() {
    echo -e "${YELLOW}📊 Service Status:${NC}"
    docker-compose ps
    echo ""
}

function show_logs() {
    echo -e "${YELLOW}📋 Service Logs (Press Ctrl+C to exit):${NC}"
    docker-compose logs -f
}

function clean_all() {
    echo -e "${RED}⚠️  WARNING: This will stop all services and DELETE all data!${NC}"
    read -p "Are you sure? Type 'yes' to continue: " confirmation
    
    if [ "$confirmation" = "yes" ]; then
        echo -e "${YELLOW}🧹 Cleaning up...${NC}"
        docker-compose down -v
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Cleanup complete!${NC}\n"
        else
            echo -e "${RED}❌ Cleanup failed${NC}"
            exit 1
        fi
    else
        echo -e "${YELLOW}❌ Cleanup cancelled${NC}\n"
    fi
}

function test_services() {
    echo -e "${YELLOW}🧪 Testing service connectivity...${NC}\n"
    
    # Test PostgreSQL
    echo -e "${CYAN}Testing PostgreSQL...${NC}"
    if docker-compose exec -T postgres pg_isready -U catalyst > /dev/null 2>&1; then
        echo -e "${GREEN}✅ PostgreSQL is ready${NC}"
    else
        echo -e "${RED}❌ PostgreSQL is not ready${NC}"
    fi
    
    # Test Redis
    echo -e "\n${CYAN}Testing Redis...${NC}"
    if docker-compose exec -T redis redis-cli --no-auth-warning -a catalyst_redis_password ping > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Redis is ready${NC}"
    else
        echo -e "${RED}❌ Redis is not ready${NC}"
    fi
    
    # Test Kafka
    echo -e "\n${CYAN}Testing Kafka...${NC}"
    if docker-compose exec -T kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092 > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Kafka is ready${NC}"
    else
        echo -e "${RED}❌ Kafka is not ready${NC}"
    fi
    
    # Test LocalStack
    echo -e "\n${CYAN}Testing LocalStack...${NC}"
    if curl -sf http://localhost:4566/_localstack/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ LocalStack is ready${NC}"
    else
        echo -e "${RED}❌ LocalStack is not ready${NC}"
    fi
    
    echo ""
}

function show_service_info() {
    echo -e "${CYAN}📍 Service URLs:${NC}"
    echo -e "  ${WHITE}Kafka UI:    http://localhost:8080${NC}"
    echo -e "  ${WHITE}PostgreSQL:  localhost:5432${NC}"
    echo -e "  ${WHITE}Redis:       localhost:6379${NC}"
    echo -e "  ${WHITE}Kafka:       localhost:9094${NC}"
    echo -e "  ${WHITE}LocalStack:  http://localhost:4566${NC}\n"
}

# Main execution
show_header

case "${1:-help}" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    clean)
        clean_all
        ;;
    init)
        init_environment
        ;;
    test)
        test_services
        ;;
    help|*)
        show_help
        ;;
esac


