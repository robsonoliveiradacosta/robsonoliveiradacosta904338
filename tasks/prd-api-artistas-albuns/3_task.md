# Tarefa 3.0: Criar migrations Flyway (schema + dados iniciais)

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Criar scripts de migração Flyway para criação do schema do banco de dados e população com dados iniciais de exemplo. As migrations devem criar todas as tabelas, índices e constraints necessárias.

<requirements>
- Usar convenção de nomenclatura Flyway: V1__description.sql, V2__description.sql
- Criar todas as tabelas conforme diagrama ER na techspec
- Incluir dados iniciais de artistas e álbuns de exemplo
- Criar usuário admin e user para testes
- Scripts devem ser idempotentes quando possível
</requirements>

## Subtarefas

- [x] 3.1 Criar V1__create_artists_table.sql
- [x] 3.2 Criar V2__create_albums_table.sql
- [x] 3.3 Criar V3__create_album_artist_junction.sql (tabela de junção N:N)
- [x] 3.4 Criar V4__create_album_images_table.sql
- [x] 3.5 Criar V5__create_users_table.sql
- [x] 3.6 Criar V6__create_regionals_table.sql
- [x] 3.7 Criar V7__insert_sample_artists.sql (dados de exemplo)
- [x] 3.8 Criar V8__insert_sample_albums.sql (dados de exemplo)
- [x] 3.9 Criar V9__insert_sample_users.sql (admin e user de teste)
- [x] 3.10 Testar migrations com banco PostgreSQL local ou container
- [x] 3.11 Verificar que a aplicação inicia: `./mvnw quarkus:dev`

## Detalhes de Implementação

Consultar o **"Diagrama ER"** na techspec.md para a estrutura das tabelas.

**Estrutura das tabelas:**

```sql
-- artists
CREATE TABLE artists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL -- SINGER ou BAND
);

-- albums
CREATE TABLE albums (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    year INTEGER NOT NULL
);

-- album_artist (junção N:N)
CREATE TABLE album_artist (
    album_id BIGINT REFERENCES albums(id),
    artist_id BIGINT REFERENCES artists(id),
    PRIMARY KEY (album_id, artist_id)
);

-- album_images
CREATE TABLE album_images (
    album_id BIGINT REFERENCES albums(id),
    image_key VARCHAR(500) NOT NULL
);

-- users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL -- ADMIN ou USER
);

-- regionals
CREATE TABLE regionals (
    id INTEGER PRIMARY KEY, -- ID externo
    name VARCHAR(200) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

**Dados iniciais sugeridos:**
- Artistas: Queen (BAND), Freddie Mercury (SINGER), Michael Jackson (SINGER)
- Álbuns: A Night at the Opera (1975), Thriller (1982)
- Usuários: admin/admin123 (ADMIN), user/user123 (USER)

## Critérios de Sucesso

- [x] 9 arquivos de migration criados em `src/main/resources/db/migration/`
- [x] Migrations executam sem erros em banco PostgreSQL limpo
- [x] Tabelas criadas com constraints corretas (FK, PK, UNIQUE)
- [x] Dados de exemplo inseridos corretamente
- [x] Aplicação inicia sem erros de schema

## Arquivos relevantes

- `src/main/resources/db/migration/V1__create_artists_table.sql`
- `src/main/resources/db/migration/V2__create_albums_table.sql`
- `src/main/resources/db/migration/V3__create_album_artist_junction.sql`
- `src/main/resources/db/migration/V4__create_album_images_table.sql`
- `src/main/resources/db/migration/V5__create_users_table.sql`
- `src/main/resources/db/migration/V6__create_regionals_table.sql`
- `src/main/resources/db/migration/V7__insert_sample_artists.sql`
- `src/main/resources/db/migration/V8__insert_sample_albums.sql`
- `src/main/resources/db/migration/V9__insert_sample_users.sql`
