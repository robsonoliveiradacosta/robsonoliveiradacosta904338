# Tarefa 13.0: Reestruturar Tabela album_images com Metadados

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será
invalidada</critical>

## Visão Geral

Atualizar a estrutura da tabela `album_images` para armazenar metadados adicionais das imagens (bucket, hash, content_type, size) e modificar a geração do identificador único de acordo com o novo padrão baseado em data.

<requirements>
- Remover coluna `image_key` da tabela `album_images`
- Adicionar coluna `bucket VARCHAR(255)` para nome do bucket no MinIO
- Adicionar coluna `hash VARCHAR(255)` para o caminho único no formato `yyyy/MM/dd/uuid.ext`
- Adicionar coluna `content_type VARCHAR(255)` para o MIME type
- Adicionar coluna `size INT` para o tamanho em bytes
- Atualizar a entidade JPA para refletir a nova estrutura
- Modificar o ImageService para usar o novo padrão de geração de hash
- Atualizar DTOs e endpoints relacionados
- Manter compatibilidade com testes existentes
</requirements>

## Subtarefas

- [ ] 13.1 Criar migration V10 para alterar a tabela `album_images`
  - Adicionar novas colunas (`bucket`, `hash`, `content_type`, `size`)
  - Migrar dados existentes de `image_key` para `hash`
  - Remover coluna `image_key`
  - Criar índice em `hash` para buscas eficientes

- [ ] 13.2 Criar entidade `AlbumImage` como classe JPA separada
  - Transformar `@ElementCollection` em `@OneToMany` com entidade própria
  - Incluir campos: `id`, `albumId`, `bucket`, `hash`, `contentType`, `size`
  - Criar AlbumImageRepository se necessário

- [ ] 13.3 Atualizar entidade `Album`
  - Substituir `@ElementCollection` por relacionamento `@OneToMany` com `AlbumImage`
  - Atualizar getters/setters

- [ ] 13.4 Atualizar `ImageService` com novo padrão de hash
  - Implementar método `getExtension(String fileName)`
  - Implementar método `generateHash(String extension)` no formato `yyyy/MM/dd/uuid.ext`
  - Modificar `uploadImage()` para salvar todos os metadados
  - Atualizar `getPresignedUrl()` para usar `hash` em vez de `imageKey`
  - Atualizar `deleteImage()` para usar nova estrutura

- [ ] 13.5 Atualizar DTOs de resposta
  - Modificar `ImageUploadResponse` para incluir novos campos
  - Modificar `ImageUrlResponse` se necessário
  - Criar DTO para representar metadados da imagem

- [ ] 13.6 Atualizar `ImageResource`
  - Ajustar endpoints para trabalhar com `hash` em vez de `imageKey`
  - Atualizar path parameters se necessário

- [ ] 13.7 Atualizar testes
  - Ajustar `ImageServiceTest` para novos métodos e estrutura
  - Ajustar `ImageResourceTest` para novos endpoints/respostas
  - Garantir cobertura dos novos cenários

## Detalhes de Implementação

### Novo Padrão de Geração de Hash

Conforme especificado no `prompt_task_13.md`:

```java
private String getExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
        return "";
    }
    return fileName.substring(fileName.lastIndexOf("."));
}

private String generateHash(String extension) {
    LocalDate now = LocalDate.now();
    String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    String uuid = UUID.randomUUID().toString();
    return String.format("%s/%s%s", datePath, uuid, extension);
}
```

**Formato do hash:** `yyyy/MM/dd/uuid.extensão`
**Exemplo:** `2026/02/02/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg`

### Nova Estrutura da Tabela

```sql
CREATE TABLE album_images (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL,
    bucket VARCHAR(255) NOT NULL,
    hash VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size INT NOT NULL,
    CONSTRAINT fk_album_images_album FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE
);

CREATE INDEX idx_album_images_album_id ON album_images(album_id);
CREATE INDEX idx_album_images_hash ON album_images(hash);
```

### Nova Entidade AlbumImage

```java
@Entity
@Table(name = "album_images")
public class AlbumImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(nullable = false, length = 255)
    private String bucket;

    @Column(nullable = false, length = 255)
    private String hash;

    @Column(name = "content_type", nullable = false, length = 255)
    private String contentType;

    @Column(nullable = false)
    private Integer size;
}
```

Referência completa: `techspec.md` seção "Modelos de Dados" e "Pontos de Integração - MinIO"

## Critérios de Sucesso

- [ ] Migration V10 executa sem erros e preserva dados existentes (se houver)
- [ ] Entidade `AlbumImage` criada e mapeada corretamente
- [ ] Relacionamento `Album` -> `AlbumImage` funcionando (`@OneToMany`)
- [ ] Upload de imagens gera hash no formato `yyyy/MM/dd/uuid.ext`
- [ ] Metadados (bucket, contentType, size) são salvos corretamente no banco
- [ ] Presigned URLs funcionam com o novo campo `hash`
- [ ] Deleção de imagens funciona com a nova estrutura
- [ ] Todos os testes passando
- [ ] Cobertura de testes mantida em >= 80%

## Arquivos Relevantes

### Arquivos a Modificar
- `src/main/resources/db/migration/V4__create_album_images_table.sql` (referência)
- `src/main/java/com/quarkus/entity/Album.java`
- `src/main/java/com/quarkus/service/ImageService.java`
- `src/main/java/com/quarkus/resource/ImageResource.java`
- `src/main/java/com/quarkus/dto/response/ImageUploadResponse.java`
- `src/main/java/com/quarkus/dto/response/ImageUrlResponse.java`
- `src/test/java/com/quarkus/service/ImageServiceTest.java`
- `src/test/java/com/quarkus/resource/ImageResourceTest.java`

### Arquivos a Criar
- `src/main/resources/db/migration/V10__alter_album_images_table.sql`
- `src/main/java/com/quarkus/entity/AlbumImage.java`
- `src/main/java/com/quarkus/repository/AlbumImageRepository.java` (opcional)
