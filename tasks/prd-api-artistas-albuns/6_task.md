# Tarefa 6.0: Implementar autenticação JWT

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Implementar autenticação e autorização JWT usando SmallRye JWT. Inclui geração de tokens, validação, refresh token, dois perfis de acesso (ADMIN e USER) e proteção dos endpoints existentes.

<requirements>
- Usar SmallRye JWT para geração e validação de tokens
- Token com expiração de 5 minutos
- Implementar endpoint de refresh token
- Dois perfis: ADMIN (CRUD completo) e USER (apenas leitura)
- Gerar par de chaves RSA para assinatura
- Hash de senhas com BCrypt
- Proteger endpoints com @RolesAllowed
</requirements>

## Subtarefas

- [ ] 6.1 Gerar par de chaves RSA (privateKey.pem e publicKey.pem)
- [ ] 6.2 Criar `UserRepository` extends `PanacheRepository<User>`
- [ ] 6.3 Criar `TokenService` para geração de JWT
- [ ] 6.4 Criar `AuthService` com lógica de login e refresh
- [ ] 6.5 Criar DTOs: `LoginRequest`, `TokenResponse`
- [ ] 6.6 Criar `AuthResource` com endpoints /login e /refresh
- [ ] 6.7 Configurar SmallRye JWT no application.properties
- [ ] 6.8 Adicionar @RolesAllowed em ArtistResource (ADMIN para escrita, USER+ADMIN para leitura)
- [ ] 6.9 Adicionar @RolesAllowed em AlbumResource
- [ ] 6.10 Criar handler para erros 401/403
- [ ] 6.11 Criar testes unitários para TokenService e AuthService
- [ ] 6.12 Criar testes de integração para AuthResource
- [ ] 6.13 Testar proteção dos endpoints manualmente

## Detalhes de Implementação

Consultar a seção **"Segurança > JWT com SmallRye"** na techspec.md para detalhes de configuração e geração de token.

**Endpoints de autenticação:**

| Método | Path | Descrição | Acesso |
|--------|------|-----------|--------|
| POST | `/api/v1/auth/login` | Autenticação | Público |
| POST | `/api/v1/auth/refresh` | Renova token | Autenticado |

**Geração de chaves RSA:**

```bash
# Gerar chave privada
openssl genrsa -out src/main/resources/privateKey.pem 2048

# Extrair chave pública
openssl rsa -in src/main/resources/privateKey.pem -pubout -out src/main/resources/publicKey.pem
```

**Configuração application.properties:**

```properties
mp.jwt.verify.publickey.location=publicKey.pem
mp.jwt.verify.issuer=quarkus-api
smallrye.jwt.sign.key.location=privateKey.pem
smallrye.jwt.new-token.lifespan=300
```

**Exemplo de TokenService:**

```java
@ApplicationScoped
public class TokenService {
    public String generateToken(User user) {
        return Jwt.issuer("quarkus-api")
            .upn(user.getUsername())
            .groups(Set.of(user.getRole().name()))
            .expiresIn(Duration.ofMinutes(5))
            .sign();
    }
}
```

**Proteção de endpoints:**

```java
@GET
@RolesAllowed({"USER", "ADMIN"})
public List<ArtistResponse> list() { ... }

@POST
@RolesAllowed("ADMIN")
public Response create(ArtistRequest request) { ... }
```

## Critérios de Sucesso

- [ ] Login retorna JWT válido com claims corretos
- [ ] Refresh token renova expiração
- [ ] Endpoints protegidos retornam 401 sem token
- [ ] Endpoints protegidos retornam 403 com role insuficiente
- [ ] USER consegue apenas GET em artistas e álbuns
- [ ] ADMIN consegue todas as operações
- [ ] Tokens expiram após 5 minutos
- [ ] Senhas hasheadas com BCrypt no banco

## Arquivos relevantes

- `src/main/resources/privateKey.pem`
- `src/main/resources/publicKey.pem`
- `src/main/java/com/quarkus/repository/UserRepository.java`
- `src/main/java/com/quarkus/service/AuthService.java`
- `src/main/java/com/quarkus/security/TokenService.java`
- `src/main/java/com/quarkus/resource/AuthResource.java`
- `src/main/java/com/quarkus/dto/request/LoginRequest.java`
- `src/main/java/com/quarkus/dto/response/TokenResponse.java`
- `src/test/java/com/quarkus/security/TokenServiceTest.java`
- `src/test/java/com/quarkus/resource/AuthResourceTest.java`
