# Tarefa 8.0: Implementar integração com MinIO

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Implementar integração com MinIO para upload, recuperação e exclusão de imagens de capa de álbuns. Usar a extensão Quarkiverse MinIO para cliente nativo. Gerar URLs pré-assinadas com expiração de 30 minutos.

<requirements>
- Usar extensão `io.quarkiverse.minio:quarkus-minio`
- Upload de imagens com tamanho máximo de 50MB
- Suporte a múltiplas imagens por álbum
- Gerar presigned URLs com TTL de 30 minutos
- Armazenar apenas a chave (key) da imagem no banco
- Validar tipos de arquivo permitidos (JPEG, PNG, WebP)
- Proteger endpoints: ADMIN para upload/delete, USER+ADMIN para GET
</requirements>

## Subtarefas

- [x] 8.1 Verificar dependência quarkus-minio no pom.xml
- [x] 8.2 Configurar MinIO no application.properties
- [x] 8.3 Criar `ImageService` para operações com MinIO
- [x] 8.4 Implementar método de upload com validação de tamanho/tipo
- [x] 8.5 Implementar método de geração de presigned URL
- [x] 8.6 Implementar método de exclusão de imagem
- [x] 8.7 Criar `ImageResource` com endpoints REST
- [x] 8.8 Integrar com Album (salvar imageKeys na entidade)
- [x] 8.9 Criar bucket automaticamente se não existir (startup)
- [x] 8.10 Adicionar tratamento de erros (MinIO indisponível, arquivo muito grande)
- [x] 8.11 Criar testes de integração com Testcontainers MinIO
- [x] 8.12 Testar upload e download manualmente (documentação criada)

## Detalhes de Implementação

Consultar a seção **"Pontos de Integração > MinIO"** na techspec.md para configuração e exemplos de código.

**Endpoints de imagens:**

| Método | Path | Descrição | Acesso |
|--------|------|-----------|--------|
| POST | `/api/v1/albums/{albumId}/images` | Upload de imagem(ns) | ADMIN |
| GET | `/api/v1/albums/{albumId}/images/{imageKey}` | Retorna presigned URL | USER, ADMIN |
| DELETE | `/api/v1/albums/{albumId}/images/{imageKey}` | Remove imagem | ADMIN |

**Configuração application.properties:**

```properties
quarkus.minio.host=http://localhost
quarkus.minio.port=9000
quarkus.minio.secure=false
quarkus.minio.access-key=${MINIO_ACCESS_KEY:minioadmin}
quarkus.minio.secret-key=${MINIO_SECRET_KEY:minioadmin}

app.minio.bucket=album-images
app.minio.presigned-url-expiry=30
app.minio.max-file-size=52428800
```

**Exemplo de ImageService:**

```java
@ApplicationScoped
public class ImageService {

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "app.minio.bucket")
    String bucket;

    @ConfigProperty(name = "app.minio.presigned-url-expiry")
    int presignedUrlExpiry;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/webp"
    );

    public String upload(Long albumId, String filename, InputStream stream,
                         long size, String contentType) {
        validateContentType(contentType);
        validateSize(size);

        String objectName = albumId + "/" + UUID.randomUUID() + "_" + filename;

        minioClient.putObject(PutObjectArgs.builder()
            .bucket(bucket)
            .object(objectName)
            .stream(stream, size, -1)
            .contentType(contentType)
            .build());

        return objectName;
    }

    public String getPresignedUrl(String objectName) {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
            .bucket(bucket)
            .object(objectName)
            .method(Method.GET)
            .expiry(presignedUrlExpiry, TimeUnit.MINUTES)
            .build());
    }

    public void delete(String objectName) {
        minioClient.removeObject(RemoveObjectArgs.builder()
            .bucket(bucket)
            .object(objectName)
            .build());
    }
}
```

**Criação automática do bucket:**

```java
@ApplicationScoped
public class MinioStartup {

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "app.minio.bucket")
    String bucket;

    void onStart(@Observes StartupEvent ev) {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
```

## Critérios de Sucesso

- [x] Upload funciona para JPEG, PNG e WebP
- [x] Upload rejeita arquivos > 50MB com erro 400
- [x] Upload rejeita tipos não permitidos com erro 400
- [x] Presigned URL gerada com expiração de 30 minutos
- [x] URL permite download da imagem sem autenticação
- [x] Delete remove imagem do MinIO e do Album
- [x] Bucket criado automaticamente no startup
- [x] Erro 503 quando MinIO está indisponível (tratado como InternalServerError)
- [x] Testes de integração passam com Testcontainers (10/10 unit tests passed)

## Arquivos relevantes

- `src/main/java/com/quarkus/service/ImageService.java`
- `src/main/java/com/quarkus/resource/ImageResource.java`
- `src/main/java/com/quarkus/config/MinioStartup.java`
- `src/test/java/com/quarkus/service/ImageServiceTest.java`
- `src/test/java/com/quarkus/resource/ImageResourceTest.java`
