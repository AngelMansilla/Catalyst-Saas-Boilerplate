# Catalyst Infrastructure Management Script
# PowerShell script for managing Docker Compose services

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('start', 'stop', 'restart', 'status', 'logs', 'clean', 'init', 'test', 'help')]
    [string]$Command = 'help'
)

$ErrorActionPreference = "Stop"
$InfraDir = Split-Path -Parent $MyInvocation.MyCommand.Path

function Show-Header {
    Write-Host ""
    Write-Host "[Catalyst] Infrastructure Manager" -ForegroundColor Cyan
    Write-Host ""
}

function Show-Help {
    Write-Host "Usage: .\catalyst.ps1 [command]" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Commands:" -ForegroundColor Green
    Write-Host "  start     - Start all services" -ForegroundColor White
    Write-Host "  stop      - Stop all services" -ForegroundColor White
    Write-Host "  restart   - Restart all services" -ForegroundColor White
    Write-Host "  status    - Show service status" -ForegroundColor White
    Write-Host "  logs      - Show service logs (follow mode)" -ForegroundColor White
    Write-Host "  clean     - Stop and remove all volumes (WARNING: deletes data)" -ForegroundColor White
    Write-Host "  init      - Initialize environment file" -ForegroundColor White
    Write-Host "  test      - Test service connectivity" -ForegroundColor White
    Write-Host "  help      - Show this help message" -ForegroundColor White
    Write-Host ""
}

function Initialize-Environment {
    Write-Host "[INIT] Initializing environment..." -ForegroundColor Yellow
    
    if (Test-Path "$InfraDir\.env") {
        Write-Host "[WARN] .env file already exists. Skipping..." -ForegroundColor Yellow
    } else {
        Copy-Item "$InfraDir\env.example" "$InfraDir\.env"
        Write-Host "[OK] Created .env file from env.example" -ForegroundColor Green
        Write-Host "[INFO] Edit .env file to customize configuration" -ForegroundColor Cyan
        Write-Host ""
    }
}

function Start-Services {
    Write-Host "[START] Starting Catalyst services..." -ForegroundColor Yellow
    Set-Location $InfraDir
    
    if (-not (Test-Path ".env")) {
        Write-Host "[WARN] .env file not found. Creating from template..." -ForegroundColor Yellow
        Initialize-Environment
    }
    
    docker-compose up -d
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "[OK] Services started successfully!" -ForegroundColor Green
        Write-Host "[INFO] Run '.\catalyst.ps1 status' to check service health" -ForegroundColor Cyan
        Write-Host ""
        Show-ServiceInfo
    } else {
        Write-Host "[ERROR] Failed to start services" -ForegroundColor Red
        exit 1
    }
}

function Stop-Services {
    Write-Host "[STOP] Stopping Catalyst services..." -ForegroundColor Yellow
    Set-Location $InfraDir
    docker-compose down
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Services stopped successfully!" -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host "[ERROR] Failed to stop services" -ForegroundColor Red
        exit 1
    }
}

function Restart-Services {
    Write-Host "[RESTART] Restarting Catalyst services..." -ForegroundColor Yellow
    Stop-Services
    Start-Sleep -Seconds 2
    Start-Services
}

function Show-Status {
    Write-Host "[STATUS] Service Status:" -ForegroundColor Yellow
    Set-Location $InfraDir
    docker-compose ps
    Write-Host ""
}

function Show-Logs {
    Write-Host "[LOGS] Service Logs (Press Ctrl+C to exit):" -ForegroundColor Yellow
    Set-Location $InfraDir
    docker-compose logs -f
}

function Clean-All {
    Write-Host "[WARNING] This will stop all services and DELETE all data!" -ForegroundColor Red
    $confirmation = Read-Host "Are you sure? Type 'yes' to continue"
    
    if ($confirmation -eq 'yes') {
        Write-Host "[CLEAN] Cleaning up..." -ForegroundColor Yellow
        Set-Location $InfraDir
        docker-compose down -v
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[OK] Cleanup complete!" -ForegroundColor Green
            Write-Host ""
        } else {
            Write-Host "[ERROR] Cleanup failed" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "[CANCEL] Cleanup cancelled" -ForegroundColor Yellow
        Write-Host ""
    }
}

function Test-Services {
    Write-Host "[TEST] Testing service connectivity..." -ForegroundColor Yellow
    Write-Host ""
    Set-Location $InfraDir
    
    # Test PostgreSQL
    Write-Host "Testing PostgreSQL..." -ForegroundColor Cyan
    docker-compose exec -T postgres pg_isready -U catalyst
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] PostgreSQL is ready" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] PostgreSQL is not ready" -ForegroundColor Red
    }
    
    # Test Redis
    Write-Host ""
    Write-Host "Testing Redis..." -ForegroundColor Cyan
    docker-compose exec -T redis redis-cli --no-auth-warning -a catalyst_redis_password ping
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Redis is ready" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Redis is not ready" -ForegroundColor Red
    }
    
    # Test Kafka
    Write-Host ""
    Write-Host "Testing Kafka..." -ForegroundColor Cyan
    docker-compose exec -T kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Kafka is ready" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Kafka is not ready" -ForegroundColor Red
    }
    
    # Test LocalStack
    Write-Host ""
    Write-Host "Testing LocalStack..." -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:4566/_localstack/health" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "[OK] LocalStack is ready" -ForegroundColor Green
        }
    } catch {
        Write-Host "[ERROR] LocalStack is not ready" -ForegroundColor Red
    }
    
    Write-Host ""
}

function Show-ServiceInfo {
    Write-Host "[INFO] Service URLs:" -ForegroundColor Cyan
    Write-Host "  Kafka UI:    http://localhost:8080" -ForegroundColor White
    Write-Host "  PostgreSQL:  localhost:5432" -ForegroundColor White
    Write-Host "  Redis:       localhost:6379" -ForegroundColor White
    Write-Host "  Kafka:       localhost:9094" -ForegroundColor White
    Write-Host "  LocalStack:  http://localhost:4566" -ForegroundColor White
    Write-Host ""
}

# Main execution
Show-Header

switch ($Command) {
    'start' { Start-Services }
    'stop' { Stop-Services }
    'restart' { Restart-Services }
    'status' { Show-Status }
    'logs' { Show-Logs }
    'clean' { Clean-All }
    'init' { Initialize-Environment }
    'test' { Test-Services }
    'help' { Show-Help }
    default { Show-Help }
}


