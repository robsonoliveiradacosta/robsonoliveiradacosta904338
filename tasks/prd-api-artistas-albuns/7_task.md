# Tarefa 7.0: Implementar Rate Limiting com Bucket4j

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Implementar rate limiting para limitar requisições a 10 por minuto por usuário autenticado. Usar a biblioteca Bucket4j com filtro JAX-RS para interceptar requisições e retornar HTTP 429 quando limite excedido.

<requirements>
- Limite de 10 requisições por minuto por usuário
- Usar Bucket4j para controle de rate limiting
- Implementar como filtro JAX-RS (@Provider)
- Retornar HTTP 429 (Too Many Requests) com mensagem adequada
- Rate limit baseado no username do JWT
- Incluir headers de rate limit na resposta (X-RateLimit-*)
</requirements>

## Subtarefas

- [x] 7.1 Verificar dependência bucket4j-core no pom.xml
- [x] 7.2 Criar `RateLimitFilter` implements `ContainerRequestFilter`
- [x] 7.3 Implementar lógica de bucket por usuário
- [x] 7.4 Configurar bandwidth: 10 tokens, refill a cada 1 minuto
- [x] 7.5 Retornar 429 com ErrorResponse quando limite excedido
- [x] 7.6 Adicionar headers X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset
- [x] 7.7 Criar testes unitários para lógica do bucket
- [x] 7.8 Criar testes de integração para verificar rate limiting
- [x] 7.9 Testar manualmente com múltiplas requisições rápidas

## Detalhes de Implementação

Consultar a seção **"Segurança > Rate Limiting com Bucket4j"** na techspec.md para o exemplo de implementação.

**Implementação do filtro:**

```java
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
public class RateLimitFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final int LIMIT = 10;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private Bucket createBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(LIMIT, Refill.intervally(LIMIT, REFILL_PERIOD)))
            .build();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Principal principal = requestContext.getSecurityContext().getUserPrincipal();
        if (principal == null) {
            return; // Skip for unauthenticated requests
        }

        String userId = principal.getName();
        Bucket bucket = buckets.computeIfAbsent(userId, k -> createBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            requestContext.abortWith(Response.status(429)
                .header("X-RateLimit-Limit", LIMIT)
                .header("X-RateLimit-Remaining", 0)
                .header("X-RateLimit-Reset", probe.getNanosToWaitForRefill() / 1_000_000_000)
                .entity(new ErrorResponse(429, "Too Many Requests", ...))
                .build());
        } else {
            // Store remaining for response filter
            requestContext.setProperty("rateLimitRemaining", probe.getRemainingTokens());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Long remaining = (Long) requestContext.getProperty("rateLimitRemaining");
        if (remaining != null) {
            responseContext.getHeaders().add("X-RateLimit-Limit", LIMIT);
            responseContext.getHeaders().add("X-RateLimit-Remaining", remaining);
        }
    }
}
```

**Headers de resposta:**
- `X-RateLimit-Limit`: Limite máximo (10)
- `X-RateLimit-Remaining`: Requisições restantes
- `X-RateLimit-Reset`: Segundos até reset do bucket

## Critérios de Sucesso

- [x] Filtro intercepta todas as requisições autenticadas
- [x] Após 10 requisições em 1 minuto, retorna 429
- [x] Headers X-RateLimit-* presentes nas respostas
- [x] Buckets são isolados por usuário
- [x] Requisições não autenticadas não são limitadas
- [x] Bucket reseta após 1 minuto
- [x] Testes validam comportamento do rate limiting

## Arquivos relevantes

- `src/main/java/com/quarkus/security/RateLimitFilter.java`
- `src/main/java/com/quarkus/dto/response/ErrorResponse.java`
- `src/test/java/com/quarkus/security/RateLimitFilterTest.java`
