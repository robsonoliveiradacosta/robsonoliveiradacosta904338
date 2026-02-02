# Tarefa 9.0: Implementar WebSocket para notificação de álbuns

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Implementar WebSocket usando Quarkus WebSockets Next para notificar clientes conectados quando um novo álbum é cadastrado. O broadcast deve enviar dados básicos do álbum para todos os clientes.

<requirements>
- Usar extensão `quarkus-websockets-next`
- Broadcast para todos os clientes conectados
- Disparo automático quando álbum é criado
- Payload JSON com id, title e year do álbum
- Endpoint WebSocket em `/ws/albums`
- Não requer autenticação para conectar (simplificação)
</requirements>

## Subtarefas

- [ ] 9.1 Verificar dependência quarkus-websockets-next no pom.xml
- [ ] 9.2 Criar `AlbumNotificationSocket` com @WebSocket
- [ ] 9.3 Implementar método de broadcast para todos os clientes
- [ ] 9.4 Criar record `AlbumNotification` com dados do álbum
- [ ] 9.5 Injetar socket no `AlbumService`
- [ ] 9.6 Chamar broadcast após criação de álbum no `AlbumService.create()`
- [ ] 9.7 Implementar handlers @OnOpen e @OnClose para logging
- [ ] 9.8 Criar teste de integração para WebSocket
- [ ] 9.9 Testar manualmente com cliente WebSocket (wscat, Postman)

## Detalhes de Implementação

Consultar a seção **"WebSocket"** na techspec.md para o exemplo de implementação.

**Implementação do WebSocket:**

```java
@WebSocket(path = "/ws/albums")
@ApplicationScoped
public class AlbumNotificationSocket {

    private static final Logger LOG = Logger.getLogger(AlbumNotificationSocket.class);

    @Inject
    WebSocketConnection connection;

    public record AlbumNotification(Long id, String title, Integer year) {}

    @OnOpen
    public void onOpen() {
        LOG.infof("Client connected: %s", connection.id());
    }

    @OnClose
    public void onClose() {
        LOG.infof("Client disconnected: %s", connection.id());
    }

    public void notifyNewAlbum(Album album) {
        AlbumNotification notification = new AlbumNotification(
            album.getId(),
            album.getTitle(),
            album.getYear()
        );
        connection.broadcast().sendTextAndAwait(notification);
        LOG.infof("Broadcast sent for album: %s", album.getTitle());
    }
}
```

**Integração com AlbumService:**

```java
@ApplicationScoped
public class AlbumService {

    @Inject
    AlbumRepository albumRepository;

    @Inject
    AlbumNotificationSocket notificationSocket;

    @Transactional
    public Album create(AlbumRequest request) {
        Album album = new Album();
        album.setTitle(request.title());
        album.setYear(request.year());
        // ... set artists

        albumRepository.persist(album);

        // Notificar clientes WebSocket
        notificationSocket.notifyNewAlbum(album);

        return album;
    }
}
```

**Teste manual com wscat:**

```bash
# Instalar wscat
npm install -g wscat

# Conectar ao WebSocket
wscat -c ws://localhost:8080/ws/albums

# Em outro terminal, criar álbum via API
curl -X POST http://localhost:8080/api/v1/albums \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "New Album", "year": 2024, "artistIds": [1]}'

# Verificar mensagem recebida no wscat
```

**Payload de notificação:**

```json
{
  "id": 123,
  "title": "New Album",
  "year": 2024
}
```

## Critérios de Sucesso

- [ ] Clientes conseguem conectar em `/ws/albums`
- [ ] Criação de álbum dispara broadcast para todos os clientes
- [ ] Payload contém id, title e year
- [ ] Múltiplos clientes recebem a mesma notificação
- [ ] Conexões/desconexões são logadas
- [ ] Não há memory leak com muitas conexões
- [ ] Teste de integração valida o fluxo completo

## Arquivos relevantes

- `src/main/java/com/quarkus/websocket/AlbumNotificationSocket.java`
- `src/main/java/com/quarkus/service/AlbumService.java` (modificar)
- `src/test/java/com/quarkus/websocket/AlbumNotificationSocketTest.java`
