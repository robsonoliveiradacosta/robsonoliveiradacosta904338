# Tarefa 12.0: Criar Docker Compose e configuração de deploy

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Criar configuração completa de Docker Compose para orquestrar todos os serviços: aplicação Quarkus, PostgreSQL e MinIO. Incluir configurações de rede, volumes, variáveis de ambiente e scripts de inicialização.

<requirements>
- Docker Compose com 3 serviços: app, postgres, minio
- Rede interna para comunicação entre containers
- Volumes persistentes para dados do banco e MinIO
- Variáveis de ambiente configuráveis
- Health checks para todos os serviços
- Ordem de inicialização correta (depends_on)
- Script para build e execução
</requirements>

## Subtarefas

- [ ] 12.1 Criar arquivo docker-compose.yml na raiz do projeto
- [ ] 12.2 Configurar serviço PostgreSQL com volume persistente
- [ ] 12.3 Configurar serviço MinIO com volume e console web
- [ ] 12.4 Configurar serviço da aplicação Quarkus
- [ ] 12.5 Configurar rede interna (bridge)
- [ ] 12.6 Criar arquivo .env.example com variáveis de ambiente
- [ ] 12.7 Adicionar health checks em todos os serviços
- [ ] 12.8 Criar script start.sh para build e execução
- [ ] 12.9 Atualizar Dockerfile.jvm se necessário
- [ ] 12.10 Testar subida completa com `docker-compose up`
- [ ] 12.11 Verificar que aplicação conecta no banco e MinIO
- [ ] 12.12 Documentar comandos no README ou CLAUDE.md

## Detalhes de Implementação

**docker-compose.yml:**

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: quarkus-api-postgres
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-quarkus_api}
      POSTGRES_USER: ${POSTGRES_USER:-quarkus}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-quarkus123}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - quarkus-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-quarkus}"]
      interval: 10s
      timeout: 5s
      retries: 5

  minio:
    image: minio/minio:latest
    container_name: quarkus-api-minio
    environment:
      MINIO_ROOT_USER: ${MINIO_ACCESS_KEY:-minioadmin}
      MINIO_ROOT_PASSWORD: ${MINIO_SECRET_KEY:-minioadmin}
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data
    networks:
      - quarkus-network
    command: server /data --console-address ":9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build:
      context: .
      dockerfile: src/main/docker/Dockerfile.jvm
    container_name: quarkus-api-app
    environment:
      QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-quarkus_api}
      QUARKUS_DATASOURCE_USERNAME: ${POSTGRES_USER:-quarkus}
      QUARKUS_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-quarkus123}
      QUARKUS_MINIO_HOST: http://minio
      QUARKUS_MINIO_PORT: 9000
      QUARKUS_MINIO_ACCESS_KEY: ${MINIO_ACCESS_KEY:-minioadmin}
      QUARKUS_MINIO_SECRET_KEY: ${MINIO_SECRET_KEY:-minioadmin}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS:-http://localhost:3000}
    ports:
      - "8080:8080"
    networks:
      - quarkus-network
    depends_on:
      postgres:
        condition: service_healthy
      minio:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/q/health/live"]
      interval: 10s
      timeout: 5s
      retries: 5

networks:
  quarkus-network:
    driver: bridge

volumes:
  postgres_data:
  minio_data:
```

**.env.example:**

```env
# PostgreSQL
POSTGRES_DB=quarkus_api
POSTGRES_USER=quarkus
POSTGRES_PASSWORD=quarkus123

# MinIO
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# Application
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

**start.sh:**

```bash
#!/bin/bash

set -e

echo "Building application..."
./mvnw package -DskipTests

echo "Starting services..."
docker-compose up --build -d

echo "Waiting for services to be healthy..."
sleep 10

echo "Checking health..."
curl -s http://localhost:8080/q/health | jq .

echo ""
echo "Services started successfully!"
echo "- API: http://localhost:8080"
echo "- Swagger UI: http://localhost:8080/q/swagger-ui"
echo "- MinIO Console: http://localhost:9001"
echo ""
echo "To view logs: docker-compose logs -f"
echo "To stop: docker-compose down"
```

**Atualização do Dockerfile.jvm (se necessário):**

Verificar se o Dockerfile existente está adequado. Pode ser necessário adicionar:

```dockerfile
# Adicionar curl para healthcheck
RUN microdnf install curl -y && microdnf clean all
```

## Critérios de Sucesso

- [ ] `docker-compose up` inicia todos os serviços sem erros
- [ ] PostgreSQL acessível na porta 5432
- [ ] MinIO acessível nas portas 9000 (API) e 9001 (Console)
- [ ] Aplicação acessível na porta 8080
- [ ] Health checks passam para todos os serviços
- [ ] Aplicação conecta no PostgreSQL automaticamente
- [ ] Aplicação conecta no MinIO automaticamente
- [ ] Migrations Flyway executam no startup
- [ ] Volumes persistem dados entre restarts
- [ ] .env.example documenta todas as variáveis

## Arquivos relevantes

- `docker-compose.yml`
- `.env.example`
- `start.sh`
- `src/main/docker/Dockerfile.jvm`
- `CLAUDE.md` (documentar comandos)
