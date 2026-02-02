# Implementar uma API REST que disponibilize dados sobre artistas e álbuns, conforme os exemplos:

- Serj Tankian - “Harakiri”, “Black Blooms”, “The Rough Dog”
- Mike Shinoda - “The Rising Tied”, “Post Traumatic”, “Post Traumatic EP”, “Where’d You Go”
- Michel Teló - “Bem Sertanejo”, “Bem Sertanejo - O Show (Ao Vivo)”, “Bem Sertanejo - (1a Temporada) - EP”
- Guns N’ Roses - “Use Your Illusion I”, “Use Your Illusion II”, “Greatest Hits”
  b) Java (Spring Boot ou Quarkus).
  
## Requisitos Gerais:
* Segurança: bloquear acesso ao endpoint a partir de domínios fora do domínio do serviço.
* Autenticação JWT com expiração a cada 5 minutos e possibilidade de renovação.
* Implementar POST, PUT, GET, DELETE
* Paginação na consulta dos álbuns.
* Expor quais álbuns são/tem cantores e/ou bandas (consultas parametrizadas).
* Consultas por nome do artista com ordenação alfabética (asc/desc).
* Upload de uma ou mais imagens de capa do álbum.
* Armazenamento das imagens no MinIO (API S3).
* Recuperação por links pré-assinados com expiração de 30 minutos.
* Versionar endpoints.
* Flyway Migrations para criar e popular tabelas.
* Documentar endpoints com OpenAPI/Swagger.
* Health Checks e Liveness/Readiness.
* Testes unitários.
* WebSocket para notificar o front a cada novo álbum cadastrado.
* Rate limit: até 10 requisições por minuto por usuário.
* Endpoint de regionais (https://integrador-argus-api.geia.vip/v1/regionais):
  - Importar a lista para tabela interna;
  - Adicionar atributo “ativo” (regional (id integer, nome varchar(200), ativo boolean));
  - Sincronizar com menor complexidade:
    * Novo no endpoint → inserir;
    * Ausente no endpoint → inativar;
    * Atributo alterado → inativar antigo e criar novo registro.
## Instruções
- Relacionamento Artista-Álbum N:N
- Criar e empacotar aplicação como imagens Docker
- Entregar como containers orquestrados (API + MinIO + BD) via docker-compose
- README.md com documentação, e como executar/testar.