# Tarefa 1.0: Configurar dependências Maven e application.properties

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Configurar o projeto Quarkus com todas as dependências necessárias no `pom.xml` e criar a estrutura base do `application.properties` com as configurações para todos os ambientes.

<requirements>
- Adicionar todas as dependências listadas na techspec.md
- Configurar application.properties com placeholders para variáveis de ambiente
- Manter compatibilidade com Quarkus 3.31.1 e Java 21
- Não quebrar o build existente
</requirements>

## Subtarefas

- [ ] 1.1 Adicionar dependências de persistência (hibernate-orm-panache, jdbc-postgresql, flyway)
- [ ] 1.2 Adicionar dependências de segurança (smallrye-jwt, smallrye-jwt-build)
- [ ] 1.3 Adicionar dependências de WebSocket (websockets-next)
- [ ] 1.4 Adicionar dependências de integração (quarkus-minio, rest-client-jackson, scheduler)
- [ ] 1.5 Adicionar dependências de observabilidade (smallrye-openapi, smallrye-health)
- [ ] 1.6 Adicionar dependências de validação (hibernate-validator)
- [ ] 1.7 Adicionar dependência de rate limiting (bucket4j-core)
- [ ] 1.8 Adicionar dependências de teste (rest-assured, testcontainers)
- [ ] 1.9 Configurar application.properties com todas as propriedades necessárias
- [ ] 1.10 Verificar que o build compila sem erros (`./mvnw compile`)

## Detalhes de Implementação

Consultar a seção **"Dependências Técnicas > Dependências Maven a adicionar"** na techspec.md para a lista completa de dependências.

Consultar as seções de configuração na techspec.md:
- **"Pontos de Integração > MinIO > Configuração"**
- **"Pontos de Integração > API Externa de Regionais > Configuração"**
- **"Segurança > JWT com SmallRye > Configuração"**
- **"Segurança > CORS"**

## Critérios de Sucesso

- [ ] Build compila sem erros: `./mvnw compile`
- [ ] Testes existentes continuam passando: `./mvnw test`
- [ ] Todas as 15+ dependências adicionadas ao pom.xml
- [ ] application.properties contém todas as configurações necessárias com valores default para dev

## Arquivos relevantes

- `pom.xml`
- `src/main/resources/application.properties`
