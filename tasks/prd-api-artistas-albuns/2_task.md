# Tarefa 2.0: Criar entidades JPA (Artist, Album, User, Regional)

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Criar todas as entidades JPA do domínio seguindo o diagrama ER e os modelos definidos na techspec. Inclui enums, relacionamentos N:N e configurações de mapeamento.

<requirements>
- Seguir estrutura de pacotes: `com.quarkus.entity`
- Usar anotações JPA padrão (@Entity, @Table, @Id, etc.)
- Implementar relacionamento N:N entre Artist e Album via tabela de junção
- Criar enums ArtistType (SINGER, BAND) e UserRole (ADMIN, USER)
- Seguir naming conventions: PascalCase para classes, camelCase para atributos
</requirements>

## Subtarefas

- [ ] 2.1 Criar enum `ArtistType` (SINGER, BAND)
- [ ] 2.2 Criar enum `UserRole` (ADMIN, USER)
- [ ] 2.3 Criar entidade `Artist` com relacionamento N:N para Album
- [ ] 2.4 Criar entidade `Album` com relacionamento N:N para Artist e lista de imageKeys
- [ ] 2.5 Criar entidade `User` com campos username, passwordHash e role
- [ ] 2.6 Criar entidade `Regional` com id externo, nome e flag active
- [ ] 2.7 Verificar que o build compila: `./mvnw compile`

## Detalhes de Implementação

Consultar a seção **"Design de Implementação > Modelos de Dados > Entidades JPA"** na techspec.md para os modelos completos.

Consultar o **"Diagrama ER"** na techspec.md para visualizar os relacionamentos.

**Pontos de atenção:**
- Album é o lado owner do relacionamento N:N (possui @JoinTable)
- Artist usa `mappedBy = "artists"` no @ManyToMany
- Album.imageKeys usa @ElementCollection para lista de strings
- Regional.id não é auto-generated (vem da API externa)

## Critérios de Sucesso

- [ ] 4 entidades criadas no pacote `com.quarkus.entity`
- [ ] 2 enums criados no pacote `com.quarkus.entity`
- [ ] Relacionamento N:N configurado corretamente
- [ ] Build compila sem erros: `./mvnw compile`
- [ ] Hibernate valida o schema sem warnings críticos

## Arquivos relevantes

- `src/main/java/com/quarkus/entity/Artist.java`
- `src/main/java/com/quarkus/entity/Album.java`
- `src/main/java/com/quarkus/entity/User.java`
- `src/main/java/com/quarkus/entity/Regional.java`
- `src/main/java/com/quarkus/entity/ArtistType.java`
- `src/main/java/com/quarkus/entity/UserRole.java`
