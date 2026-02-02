# PRD - API REST de Artistas e Álbuns

## Visão Geral

API REST para gerenciamento de catálogo musical contendo artistas e álbuns. O projeto serve como demonstração de habilidades técnicas em desenvolvimento backend com Quarkus, implementando boas práticas de segurança, arquitetura e DevOps.

A API permite operações CRUD completas, autenticação JWT, upload de imagens para MinIO, notificações em tempo real via WebSocket e integração com serviço externo de regionais.

## Objetivos

- Demonstrar proficiência em Quarkus e ecossistema Java moderno
- Implementar autenticação e autorização JWT com dois níveis de acesso
- Aplicar boas práticas de API REST (versionamento, paginação, documentação)
- Integrar serviços externos (MinIO S3, API de regionais)
- Entregar aplicação containerizada e orquestrada via Docker Compose

**Métricas de sucesso:**
- 100% dos endpoints documentados no OpenAPI/Swagger
- Cobertura de testes unitários mínima de 80%
- Health checks funcionais (liveness/readiness)
- Rate limiting operacional (10 req/min por usuário)

## Histórias de Usuário

### Administrador
- Como admin, quero cadastrar artistas para manter o catálogo atualizado
- Como admin, quero cadastrar álbuns vinculados a artistas para organizar o catálogo
- Como admin, quero fazer upload de imagens de capa para enriquecer os dados dos álbuns
- Como admin, quero sincronizar regionais do serviço externo para manter dados atualizados

### Usuário Comum
- Como usuário, quero consultar artistas por nome com ordenação para encontrar rapidamente
- Como usuário, quero listar álbuns com paginação para navegar pelo catálogo
- Como usuário, quero filtrar álbuns por tipo (cantor/banda) para refinar buscas
- Como usuário, quero receber notificações de novos álbuns via WebSocket para ficar atualizado
- Como usuário, quero acessar imagens de capa via links pré-assinados para visualizar capas

## Funcionalidades Principais

### RF01 - Gestão de Artistas
- CRUD completo de artistas (nome, tipo: cantor/banda)
- Exclusão permanente (hard delete)
- Consulta por nome com ordenação alfabética (asc/desc)
- Relacionamento N:N com álbuns

### RF02 - Gestão de Álbuns
- CRUD completo de álbuns (título, ano, artistas vinculados)
- Exclusão permanente (hard delete)
- Paginação na listagem (page, size, sort)
- Filtro por tipo de artista (cantor/banda)
- Relacionamento N:N com artistas

### RF03 - Upload de Imagens
- Upload de uma ou mais imagens de capa por álbum
- Tamanho máximo por imagem: 50MB
- Armazenamento no MinIO (compatível S3)
- Recuperação via links pré-assinados com expiração de 30 minutos

### RF04 - Autenticação e Autorização
- Autenticação JWT com expiração de 5 minutos
- Endpoint de renovação de token (refresh)
- Dois perfis: ADMIN (CRUD completo) e USER (apenas leitura)
- CORS configurado para bloquear domínios externos (domínios permitidos configuráveis via variáveis de ambiente)

### RF05 - WebSocket
- Notificação broadcast para todos os clientes conectados
- Disparo automático a cada novo álbum cadastrado
- Payload com dados básicos do álbum criado

### RF06 - Rate Limiting
- Limite de 10 requisições por minuto por usuário
- Resposta HTTP 429 (Too Many Requests) quando excedido

### RF07 - Sincronização de Regionais
- Importação de regionais do endpoint externo (integrador-argus-api)
- Estrutura: id (integer), nome (varchar 200), ativo (boolean)
- Sincronização automática diária às 04:00 + disparo manual via endpoint
- Regras de sincronização:
  - Novo no endpoint externo → inserir com ativo=true
  - Ausente no endpoint externo → inativar (ativo=false)
  - Atributo alterado → inativar antigo e criar novo registro

### RF08 - Observabilidade
- Health checks: liveness e readiness
- Documentação OpenAPI/Swagger
- Versionamento de endpoints (v1)

### RF09 - Migrations
- Flyway para criação e população inicial das tabelas
- Dados iniciais: artistas e álbuns de exemplo conforme especificação

## Experiência do Usuário

**Personas:**
- **Admin**: Responsável por manter o catálogo, realizar uploads e sincronizações
- **Usuário**: Consome a API para consultar artistas e álbuns

**Fluxos principais:**
1. Autenticação → obter token JWT
2. Consulta de artistas com filtros e ordenação
3. Listagem paginada de álbuns
4. Visualização de capas via links pré-assinados

**Interface:**
- API REST documentada via Swagger UI
- Respostas JSON padronizadas
- Códigos HTTP semânticos (200, 201, 400, 401, 403, 404, 429)

## Restrições Técnicas de Alto Nível

**Stack obrigatória:**
- Java 21 com Quarkus
- PostgreSQL como banco de dados
- MinIO para armazenamento de imagens
- Docker e Docker Compose para orquestração

**Integrações:**
- MinIO (API S3) para imagens
- API externa: https://integrador-argus-api.geia.vip/v1/regionais

**Segurança:**
- JWT com expiração de 5 minutos
- CORS restrito ao domínio da aplicação
- Rate limiting por usuário

**Performance:**
- Links pré-assinados com TTL de 30 minutos
- Paginação obrigatória em listagens

## Fora de Escopo

- Frontend/interface web
- Deploy em cloud (AWS, GCP, Azure)
- Pipeline CI/CD
- Monitoramento avançado (Prometheus, Grafana)
- Cache distribuído (Redis)
- Múltiplos idiomas (i18n)
- Notificações push mobile
- Busca full-text avançada (Elasticsearch)
