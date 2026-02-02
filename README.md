# API de Catálogo Musical

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.31.1-blue.svg)](https://quarkus.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18.1-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

API REST profissional para gerenciamento de catálogo musical, incluindo artistas, álbuns e imagens. Construída com Quarkus para máxima performance, baixo consumo de memória e inicialização rápida.

---

## Processo Seletivo

> **PROCESSO SELETIVO CONJUNTO Nº 001/2026/SEPLAG/SEFAZ/SEDUC/SESP/PJC/PMMT/CBMMT/DETRAN/POLITEC/SEJUS/SEMA/SEAF/SINFRA/SECITECI/PGE/MTPREV**

**Projeto Prático**: Implementação Back End Java Sênior

| Informação | Detalhe |
|------------|---------|
| **Candidato** | Robson Oliveira da Costa |
| **Perfil Profissional** | Engenheiro da Computação - Sênior |
| **Inscrição** | 16429 |

Este projeto foi desenvolvido como parte da avaliação técnica do processo seletivo, demonstrando competências em:
- Arquitetura de microsserviços com Quarkus
- Desenvolvimento de APIs RESTful
- Persistência de dados com JPA/Hibernate e PostgreSQL
- Segurança com JWT e controle de acesso baseado em roles
- Integração com serviços externos (MinIO para armazenamento de objetos)
- Comunicação em tempo real com WebSocket
- Testes automatizados (unitários e de integração)
- Containerização com Docker
- Boas práticas de desenvolvimento e padrões de projeto

---

## Índice

- [Visão Geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Recursos](#recursos)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Configuração](#instalação-e-configuração)
- [Executando o Projeto](#executando-o-projeto)
- [Testes](#testes)
- [Build e Empacotamento](#build-e-empacotamento)
- [Docker](#docker)
- [Documentação da API](#documentação-da-api)
- [Autenticação](#autenticação)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Contribuindo](#contribuindo)

## Visão Geral

Esta API oferece um sistema completo de gerenciamento de catálogo musical com as seguintes capacidades:

- **CRUD de Artistas e Álbuns**: Gerenciamento completo de artistas (cantores e bandas) e seus álbuns
- **Upload de Imagens**: Armazenamento e gerenciamento de capas de álbuns usando MinIO
- **Autenticação JWT**: Sistema de autenticação baseado em tokens com controle de acesso por roles
- **Sincronização Externa**: Integração com API regional externa com sincronização automática
- **Notificações em Tempo Real**: WebSocket para notificações de atualizações de álbuns
- **Paginação e Filtros**: Suporte completo para paginação, ordenação e filtros
- **Health Checks**: Monitoramento de saúde de banco de dados e MinIO
- **Rate Limiting**: Proteção contra abuso de API

## Tecnologias

### Core
- **Java 21**: Linguagem de programação moderna com recursos avançados
- **Quarkus 3.31.1**: Framework Kubernetes-native para Java otimizado para GraalVM e HotSpot
- **Maven**: Gerenciamento de dependências e build

### Persistência
- **PostgreSQL 18.1**: Banco de dados relacional robusto
- **Hibernate ORM with Panache**: Simplificação de JPA
- **Flyway**: Versionamento e migração de banco de dados

### Segurança
- **SmallRye JWT**: Autenticação e autorização baseada em JWT (RFC 7519)
- **BCrypt**: Hash seguro de senhas
- **CORS**: Configuração de Cross-Origin Resource Sharing

### Armazenamento
- **MinIO**: Armazenamento de objetos S3-compatible para imagens

### Comunicação
- **REST with Jackson**: API RESTful com serialização JSON
- **WebSocket**: Comunicação bidirecional em tempo real
- **REST Client**: Cliente HTTP declarativo para integração externa

### Documentação e Monitoramento
- **SmallRye OpenAPI**: Geração automática de especificação OpenAPI 3.0
- **Swagger UI**: Interface interativa para documentação da API
- **SmallRye Health**: Health checks para readiness e liveness probes

### Qualidade e Testes
- **JUnit 5**: Framework de testes
- **REST Assured**: Testes de API REST
- **Testcontainers**: Testes de integração com containers Docker
- **Mockito**: Framework de mocking
- **WireMock**: Simulação de APIs externas

### DevOps
- **Docker**: Containerização da aplicação
- **Docker Compose**: Orquestração de múltiplos serviços
- **GraalVM Native Image**: Compilação nativa para startup ultra-rápido

## Recursos

### API de Artistas
- Listar artistas com filtros e ordenação
- Buscar artista por ID
- Criar novo artista (ADMIN)
- Atualizar artista existente (ADMIN)
- Remover artista (ADMIN)

### API de Álbuns
- Listar álbuns com paginação, filtros e ordenação
- Buscar álbum por ID
- Criar novo álbum (ADMIN)
- Atualizar álbum existente (ADMIN)
- Remover álbum (ADMIN)
- Filtrar por tipo de artista (SINGER/BAND)

### API de Imagens
- Upload de imagens de álbuns (JPEG, PNG, WebP)
- Geração de URLs pré-assinadas com expiração
- Remoção de imagens
- Validação de tipo e tamanho de arquivo (máx. 50MB)
- Armazenamento com hash único e organização por data

### API de Autenticação
- Login com credenciais (username/password)
- Geração de token JWT
- Refresh de token
- Controle de acesso baseado em roles (USER/ADMIN)

### API Regional
- Listar regionais ativas
- Sincronização manual com API externa
- Sincronização automática agendada (scheduler)

### Funcionalidades Adicionais
- Rate limiting para proteção contra abuso
- WebSocket para notificações de alterações em álbuns
- Health checks (liveness e readiness)
- Logs estruturados
- Validação de entrada com Bean Validation
- Tratamento centralizado de exceções

## Pré-requisitos

### Para Desenvolvimento Local
- **Java 21+** ([OpenJDK](https://openjdk.org/) ou [Oracle JDK](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.9+** (ou use o wrapper incluído `./mvnw`)
- **Docker** e **Docker Compose** (para serviços de infraestrutura)
- **Git** (para controle de versão)

### Para Execução via Docker
- **Docker 20.10+**
- **Docker Compose 2.0+**

## Instalação e Configuração

### 1. Clone o Repositório

```bash
git clone https://github.com/seu-usuario/quarkus-ai-02.git
cd quarkus-ai-02
```

### 2. Configure as Variáveis de Ambiente

Copie o arquivo de exemplo e ajuste conforme necessário:

```bash
cp .env.example .env
```

Edite o arquivo `.env` com suas configurações:

```properties
# PostgreSQL Configuration
POSTGRES_DB=music_catalog
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# MinIO Configuration
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=album-images

# Application Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000

# External API Configuration
REGIONAL_API_URL=https://integrador-argus-api.geia.vip
```

### 3. Inicie os Serviços de Infraestrutura

Com Docker Compose:

```bash
docker compose up -d postgres minio
```

Aguarde os serviços estarem prontos (health checks):

```bash
docker compose ps
```

### 4. Gere as Chaves JWT

O projeto requer um par de chaves RSA para assinatura de tokens JWT. Crie os arquivos `privateKey.pem` e `publicKey.pem` em `src/main/resources/`:

```bash
# Gerar chave privada
openssl genrsa -out src/main/resources/privateKey.pem 2048

# Gerar chave pública
openssl rsa -in src/main/resources/privateKey.pem -pubout -out src/main/resources/publicKey.pem
```

## Executando o Projeto

### Modo Desenvolvimento (Dev Mode)

O modo de desenvolvimento do Quarkus oferece live reload automático:

```bash
./mvnw quarkus:dev
```

A aplicação estará disponível em:
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Health Checks**: http://localhost:8080/q/health
- **Dev UI**: http://localhost:8080/q/dev (apenas em modo dev)

### Modo Produção (JVM)

#### Compilar e Executar

```bash
# Compilar
./mvnw package

# Executar
java -jar target/quarkus-app/quarkus-run.jar
```

### Modo Produção (Native)

Para compilação nativa com GraalVM (requer GraalVM instalado):

```bash
# Compilar nativo
./mvnw package -Dnative

# Executar
./target/quarkus-ai-02-1.0-SNAPSHOT-runner
```

Ou usando container build (recomendado, não requer GraalVM local):

```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

**Vantagens da Compilação Nativa:**
- Startup em milissegundos
- Consumo de memória reduzido (~10x menor)
- Ideal para containers e serverless

## Testes

### Executar Todos os Testes

```bash
./mvnw test
```

### Executar Testes de Integração

```bash
./mvnw verify
```

### Executar Testes com Cobertura

```bash
./mvnw verify jacoco:report
```

O relatório de cobertura estará disponível em `target/site/jacoco/index.html`.

### Executar Teste Específico

```bash
./mvnw test -Dtest=AlbumResourceTest
```

### Testes Disponíveis

O projeto inclui:
- **Testes Unitários**: Testes de serviços e lógica de negócio
- **Testes de Integração**: Testes de API REST com Testcontainers
- **Testes de Segurança**: Validação de autenticação e autorização
- **Testes de Repository**: Validação de queries JPA
- **Mocks de API Externa**: Testes com WireMock

## Build e Empacotamento

### Build JVM (Uber-JAR)

```bash
./mvnw package
```

Artefato gerado: `target/quarkus-app/`

### Build Nativo

```bash
# Com GraalVM local
./mvnw package -Dnative

# Com container build (sem GraalVM local)
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Artefato gerado: `target/quarkus-ai-02-1.0-SNAPSHOT-runner`

### Pular Testes no Build

```bash
./mvnw package -DskipTests
```

## Docker

### Executar com Docker Compose (Recomendado)

O jeito mais fácil de executar todo o stack (PostgreSQL + MinIO + Aplicação):

```bash
# Build e start de todos os serviços
./start.sh

# Ou manualmente:
./mvnw package -DskipTests
docker compose up --build -d
```

### Ver Logs

```bash
# Todos os serviços
docker compose logs -f

# Serviço específico
docker compose logs -f app
docker compose logs -f postgres
docker compose logs -f minio
```

### Parar Serviços

```bash
# Parar serviços
docker compose down

# Parar e remover volumes (limpar dados)
docker compose down -v
```

### Verificar Saúde dos Serviços

```bash
# Health check da aplicação
curl http://localhost:8080/q/health

# Liveness
curl http://localhost:8080/q/health/live

# Readiness
curl http://localhost:8080/q/health/ready

# Status do Docker Compose
docker compose ps
```

### Build Manual de Imagens Docker

#### Imagem JVM

```bash
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/quarkus-ai-02-jvm .
docker run -i --rm -p 8080:8080 quarkus/quarkus-ai-02-jvm
```

#### Imagem Native (Micro)

```bash
./mvnw package -Dnative
docker build -f src/main/docker/Dockerfile.native-micro -t quarkus/quarkus-ai-02 .
docker run -i --rm -p 8080:8080 quarkus/quarkus-ai-02
```

### URLs dos Serviços

Quando executado com Docker Compose:

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **API** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/q/swagger-ui | - |
| **Health Checks** | http://localhost:8080/q/health | - |
| **MinIO Console** | http://localhost:9001 | minioadmin / minioadmin |
| **PostgreSQL** | localhost:5432 | postgres / postgres |
| **MinIO API** | http://localhost:9000 | minioadmin / minioadmin |

## Documentação da API

### Swagger UI (Interativo)

Acesse a documentação interativa da API:

http://localhost:8080/q/swagger-ui

### Especificação OpenAPI

Obtenha a especificação OpenAPI 3.0 em JSON:

http://localhost:8080/q/openapi

### Endpoints Principais

#### Autenticação

```bash
# Login
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

# Refresh Token
POST /api/v1/auth/refresh
Authorization: Bearer {token}
```

#### Artistas

```bash
# Listar artistas
GET /api/v1/artists?name=Beatles&sort=name:asc
Authorization: Bearer {token}

# Buscar artista
GET /api/v1/artists/{id}
Authorization: Bearer {token}

# Criar artista (ADMIN)
POST /api/v1/artists
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "The Beatles",
  "type": "BAND",
  "country": "UK"
}

# Atualizar artista (ADMIN)
PUT /api/v1/artists/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "The Beatles",
  "type": "BAND",
  "country": "United Kingdom"
}

# Deletar artista (ADMIN)
DELETE /api/v1/artists/{id}
Authorization: Bearer {token}
```

#### Álbuns

```bash
# Listar álbuns com paginação
GET /api/v1/albums?page=0&size=20&sort=title:asc&artistType=BAND
Authorization: Bearer {token}

# Buscar álbum
GET /api/v1/albums/{id}
Authorization: Bearer {token}

# Criar álbum (ADMIN)
POST /api/v1/albums
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Abbey Road",
  "year": 1969,
  "artistIds": [1]
}

# Atualizar álbum (ADMIN)
PUT /api/v1/albums/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Abbey Road (Remastered)",
  "year": 1969,
  "artistIds": [1]
}

# Deletar álbum (ADMIN)
DELETE /api/v1/albums/{id}
Authorization: Bearer {token}
```

#### Imagens

```bash
# Upload de imagem (ADMIN)
POST /api/v1/albums/{albumId}/images
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [imagem.jpg]

# Obter URL da imagem
GET /api/v1/albums/{albumId}/images/{hash}
Authorization: Bearer {token}

# Deletar imagem (ADMIN)
DELETE /api/v1/albums/{albumId}/images/{hash}
Authorization: Bearer {token}
```

#### Regionais

```bash
# Listar regionais
GET /api/v1/regionals
Authorization: Bearer {token}

# Sincronizar manualmente (ADMIN)
POST /api/v1/regionals/sync
Authorization: Bearer {token}
```

## Autenticação

A API utiliza JWT (JSON Web Tokens) para autenticação e autorização.

### Obter Token

Faça login com credenciais válidas:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Resposta:

```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 300,
  "username": "admin",
  "role": "ADMIN"
}
```

### Usar Token nas Requisições

Inclua o token no header `Authorization`:

```bash
curl -X GET http://localhost:8080/api/v1/albums \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Roles e Permissões

| Role | Descrição | Permissões |
|------|-----------|------------|
| **USER** | Usuário padrão | Leitura de artistas, álbuns e imagens |
| **ADMIN** | Administrador | Todas as operações (CRUD completo) |

### Usuários Padrão

Usuários criados automaticamente pelo Flyway:

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| user | user123 | USER |

**IMPORTANTE**: Altere as senhas em produção!

### Refresh Token

Renove um token antes que expire:

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Authorization: Bearer {current_token}"
```

## Estrutura do Projeto

```
quarkus-ai-02/
├── src/
│   ├── main/
│   │   ├── java/com/quarkus/
│   │   │   ├── config/          # Configurações e startup
│   │   │   ├── dto/             # DTOs (request/response)
│   │   │   ├── entity/          # Entidades JPA
│   │   │   ├── exception/       # Exception handlers
│   │   │   ├── health/          # Health checks customizados
│   │   │   ├── integration/     # Clientes de APIs externas
│   │   │   ├── repository/      # Repositórios Panache
│   │   │   ├── resource/        # REST endpoints
│   │   │   ├── scheduler/       # Jobs agendados
│   │   │   ├── security/        # Segurança (JWT, rate limit)
│   │   │   ├── service/         # Lógica de negócio
│   │   │   └── websocket/       # WebSocket endpoints
│   │   ├── resources/
│   │   │   ├── db/migration/    # Scripts Flyway
│   │   │   ├── application.properties
│   │   │   ├── privateKey.pem   # Chave privada JWT
│   │   │   └── publicKey.pem    # Chave pública JWT
│   │   └── docker/
│   │       ├── Dockerfile.jvm           # Imagem JVM
│   │       ├── Dockerfile.native        # Imagem nativa
│   │       └── Dockerfile.native-micro  # Imagem nativa micro
│   └── test/
│       └── java/com/quarkus/    # Testes unitários e integração
├── docker-compose.yml           # Orquestração de serviços
├── .env.example                 # Exemplo de variáveis de ambiente
├── pom.xml                      # Dependências Maven
├── CLAUDE.md                    # Instruções para Claude Code
└── README.md                    # Este arquivo
```

### Camadas da Aplicação

1. **Resource**: Endpoints REST, validação de entrada, mapeamento HTTP
2. **Service**: Lógica de negócio, orquestração, transações
3. **Repository**: Acesso a dados, queries JPA
4. **Entity**: Modelos de domínio, mapeamento JPA
5. **DTO**: Objetos de transferência de dados, contratos da API

### Padrões Utilizados

- **Repository Pattern**: Abstração do acesso a dados
- **DTO Pattern**: Separação entre modelo de domínio e API
- **Dependency Injection**: CDI para inversão de controle
- **Builder Pattern**: Construção de objetos complexos
- **Exception Handler**: Tratamento centralizado de erros

## Recursos Avançados

### WebSocket

Conecte-se ao WebSocket para receber notificações em tempo real:

```javascript
const ws = new WebSocket('ws://localhost:8080/ws/album-notifications');

ws.onmessage = (event) => {
  const notification = JSON.parse(event.data);
  console.log('Album updated:', notification);
};
```

### Rate Limiting

A API implementa rate limiting para proteção:
- Configurável via properties
- Por IP e endpoint
- Baseado em Bucket4j

### Health Checks

Endpoints de health check para Kubernetes/OpenShift:

```bash
# Liveness (a aplicação está viva?)
curl http://localhost:8080/q/health/live

# Readiness (a aplicação está pronta para tráfego?)
curl http://localhost:8080/q/health/ready

# Health completo
curl http://localhost:8080/q/health
```

### Profiles

O projeto suporta diferentes profiles:

- **dev**: Desenvolvimento local (logs debug, SQL logging)
- **test**: Execução de testes
- **prod**: Produção (logs otimizados, sem SQL logging)

Ativar profile:

```bash
# Via Maven
./mvnw quarkus:dev -Dquarkus.profile=dev

# Via Java
java -Dquarkus.profile=prod -jar target/quarkus-app/quarkus-run.jar
```

## Monitoramento e Observabilidade

### Logs

A aplicação utiliza logging estruturado:

```bash
# Ver logs em tempo real
docker compose logs -f app

# Filtrar por nível
docker compose logs -f app | grep ERROR
```

### Métricas

Health checks fornecem métricas básicas sobre:
- Status do banco de dados
- Status do MinIO
- Status da aplicação

### Troubleshooting

#### Aplicação não inicia

1. Verifique se PostgreSQL está rodando:
   ```bash
   docker compose ps postgres
   ```

2. Verifique se MinIO está rodando:
   ```bash
   docker compose ps minio
   ```

3. Verifique os logs:
   ```bash
   docker compose logs app
   ```

#### Erro de autenticação

1. Verifique se as chaves JWT existem em `src/main/resources/`
2. Verifique se o token não expirou (TTL padrão: 300 segundos)
3. Use o endpoint `/auth/refresh` para renovar

#### Erro de upload de imagem

1. Verifique se o MinIO está acessível
2. Verifique se o bucket foi criado automaticamente
3. Verifique o tamanho do arquivo (máx. 50MB)
4. Verifique o formato (JPEG, PNG, WebP)

## Performance

### Benchmarks JVM vs Native

| Métrica | JVM | Native | Melhoria |
|---------|-----|--------|----------|
| **Startup** | ~2.5s | ~0.05s | **50x** |
| **Memória RSS** | ~250MB | ~25MB | **10x** |
| **Primeira Requisição** | ~50ms | ~10ms | **5x** |
| **Build Time** | ~30s | ~3min | - |

### Otimizações

- Hibernate ORM otimizado para Quarkus
- Connection pooling configurado
- Lazy loading de relacionamentos
- Índices no banco de dados
- Presigned URLs para imagens (reduz carga no servidor)

## Contribuindo

Contribuições são bem-vindas! Por favor:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Padrões de Código

- Siga as convenções Java e Jakarta EE
- Mantenha testes para novas funcionalidades
- Use Javadoc para métodos públicos
- Valide com `mvn verify` antes de commitar

## Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## Suporte

Para questões e suporte:
- Abra uma [issue](https://github.com/seu-usuario/quarkus-ai-02/issues)
- Email: suporte@example.com

## Referências

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Jakarta EE Specifications](https://jakarta.ee/specifications/)
- [MicroProfile Specifications](https://microprofile.io/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MinIO Documentation](https://min.io/docs/)

---

Desenvolvido com Quarkus - Supersonic Subatomic Java
