# Catalyst Infrastructure

This directory contains all infrastructure configuration for Project Catalyst, including Docker Compose setup and initialization scripts.

## 📋 Overview

The infrastructure includes:
- **PostgreSQL 16**: Primary database with pre-configured schemas
- **Redis 7**: Caching layer with persistence
- **Apache Kafka 3.6**: Message queue using KRaft mode (no Zookeeper)
- **Kafka UI**: Web interface for Kafka management
- **LocalStack**: AWS S3 simulation for local development
- **Mailpit**: SMTP testing server and web UI for email development

## 🚀 Quick Start

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+
- 4GB RAM minimum
- 10GB disk space

### Starting the Infrastructure

1. **Copy environment file**:
```bash
cd infra
cp env.example .env
```

2. **Start all services**:
```bash
docker-compose up -d
```

3. **Check service health**:
```bash
docker-compose ps
```

4. **View logs**:
```bash
docker-compose logs -f
```

### Stopping the Infrastructure

```bash
docker-compose down
```

To remove volumes (⚠️ deletes all data):
```bash
docker-compose down -v
```

## 🔧 Service Details

### PostgreSQL 16
- **Port**: 5432 (configurable via `POSTGRES_PORT`)
- **Default Database**: `catalyst_db`
- **Default User**: `catalyst`
- **Schemas**: `public`, `payment`, `auth`, `audit`
- **Extensions**: `uuid-ossp`, `pgcrypto`

**Connection String**:
```
postgresql://catalyst:catalyst_dev_password@localhost:5432/catalyst_db
```

**Initialization**:
- Database schemas and tables are created automatically on first start
- Init scripts located in `postgres/init/`
- Scripts run in alphabetical order

### Redis 7
- **Port**: 6379 (configurable via `REDIS_PORT`)
- **Persistence**: AOF enabled (appendonly)
- **Max Memory**: 256MB (configurable via `REDIS_MAX_MEMORY`)
- **Eviction Policy**: allkeys-lru
- **Password**: Set via `REDIS_PASSWORD`

**Connection String**:
```
redis://:catalyst_redis_password@localhost:6379
```

### Apache Kafka 3.6
- **Port**: 9094 (external access)
- **Internal Port**: 9092 (container-to-container)
- **Mode**: KRaft (no Zookeeper required)
- **Partitions**: 3 (default)
- **Replication Factor**: 1 (single broker)
- **Auto Create Topics**: Enabled

**Bootstrap Servers**:
- External: `localhost:9094`
- Internal: `kafka:9092`

### Kafka UI
- **Port**: 8080 (configurable via `KAFKA_UI_PORT`)
- **URL**: http://localhost:8080
- **Features**:
  - Topic management
  - Message browsing
  - Consumer group monitoring
  - Cluster configuration

### LocalStack
- **Port**: 4566 (configurable via `LOCALSTACK_PORT`)
- **Services**: S3
- **Region**: us-east-1 (configurable via `AWS_REGION`)

**S3 Buckets** (auto-created):
- `catalyst-uploads`: User file uploads
- `catalyst-documents`: Document storage
- `catalyst-avatars`: User avatar images (public read)
- `catalyst-backups`: Database backups

**AWS CLI Configuration**:
```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
export AWS_ENDPOINT_URL=http://localhost:4566
```

**List buckets**:
```bash
aws --endpoint-url=http://localhost:4566 s3 ls
```

### Mailpit
- **SMTP Port**: 1025
- **Web UI Port**: 8025 (configurable via `MAILPIT_DASHBOARD_PORT`)
- **Web UI URL**: http://localhost:8025

**Features**:
- Catch-all SMTP server
- Web interface to view sent emails
- API for automated email testing

## 📊 Health Checks

All services include health checks:

```bash
# Check all services
docker-compose ps

# Check specific service
docker-compose exec postgres pg_isready
docker-compose exec redis redis-cli ping
docker-compose exec kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092
curl http://localhost:4566/_localstack/health
curl http://localhost:8025/livez
```

## 🗄️ Database Schema

### Auth Schema
- `users`: User accounts and profiles
- `sessions`: JWT refresh tokens and sessions

### Payment Schema
- `customers`: Stripe customer records
- `subscriptions`: Subscription management
- `transactions`: Payment transactions
- `webhook_events`: Stripe webhook event log

### Audit Schema
- `event_log`: System-wide audit trail

## 🔐 Security Notes

**⚠️ Development Only**:
- Default passwords are for development only
- Change all credentials for production
- Use secrets management in production
- Enable SSL/TLS for production databases

**Production Checklist**:
- [ ] Change all default passwords
- [ ] Enable SSL for PostgreSQL
- [ ] Configure Redis AUTH
- [ ] Set up Kafka SASL/SSL
- [ ] Use proper AWS credentials (not LocalStack)
- [ ] Implement network segmentation
- [ ] Enable audit logging
- [ ] Set up monitoring and alerts

## 🐛 Troubleshooting

### PostgreSQL won't start
```bash
# Check logs
docker-compose logs postgres

# Remove volume and restart
docker-compose down -v
docker-compose up -d postgres
```

### Kafka connection issues
```bash
# Verify Kafka is ready
docker-compose exec kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# Check listeners
docker-compose exec kafka kafka-configs.sh --bootstrap-server localhost:9092 --describe --entity-type brokers --entity-name 1
```

### LocalStack S3 not working
```bash
# Reinitialize buckets
docker-compose restart localstack

# Check bucket creation
aws --endpoint-url=http://localhost:4566 s3 ls
```

### Port conflicts
```bash
# Check what's using a port (example: 5432)
# Windows
netstat -ano | findstr :5432

# Linux/Mac
lsof -i :5432

# Change port in .env file
POSTGRES_PORT=5433
```

## 📈 Monitoring

### View Resource Usage
```bash
docker stats
```

### View Service Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f postgres
docker-compose logs -f kafka
```

### Database Queries
```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U catalyst -d catalyst_db

# List schemas
\dn

# List tables in payment schema
\dt payment.*

# Check audit log
SELECT * FROM audit.event_log ORDER BY created_at DESC LIMIT 10;
```

## 🔄 Backup and Restore

### Backup PostgreSQL
```bash
docker-compose exec postgres pg_dump -U catalyst catalyst_db > backup.sql
```

### Restore PostgreSQL
```bash
docker-compose exec -T postgres psql -U catalyst catalyst_db < backup.sql
```

### Backup Redis
```bash
docker-compose exec redis redis-cli --rdb /data/dump.rdb SAVE
docker cp catalyst-redis:/data/dump.rdb ./redis-backup.rdb
```

## 📚 Additional Resources

- [PostgreSQL Documentation](https://www.postgresql.org/docs/16/)
- [Redis Documentation](https://redis.io/documentation)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [LocalStack Documentation](https://docs.localstack.cloud/)

## 📝 Notes

- All services use named volumes for data persistence
- Services communicate via `catalyst-network` bridge network
- Health checks ensure services are ready before dependent services start
- Initialization scripts run only on first container creation


