# ms-matches

Microserviço responsável pela gestão de partidas do sistema. Nesta versão inicial, o serviço cobre o cadastro, consulta e atualização básica de partidas da Copa do Mundo 2026, com persistência em MongoDB e estrutura preparada para validação futura contra o `ms-core-data`.

## Escopo Atual

O `ms-matches` hoje cobre:

- Cadastro de partidas.
- Consulta de todas as partidas.
- Consulta de partida por ID.
- Filtro de partidas por status.
- Atualização de status da partida.
- Inclusão de eventos na timeline da partida.
- Persistência da partida como documento MongoDB.
- DTOs de entrada e saída para separar API REST do modelo interno.
- Mappers para conversão entre DTOs, domínio e resposta de erro.
- Tratamento centralizado de exceções REST.
- Validação opcional de seleções e estádios via um `ms-core-data` futuro.

## Modelo de Partida

O modelo foi alinhado com a API `rezarahiminia/worldcup2026`. Por isso, a partida usa campos como:

- `externalMatchId`
- `homeTeamId`
- `awayTeamId`
- `homeScore`
- `awayScore`
- `homeTeamLabel`
- `awayTeamLabel`
- `stadiumId`
- `group`
- `matchday`
- `type`
- `status`
- `finished`
- `timelineEvents`

Para jogos de mata-mata ainda indefinidos, a API externa usa `home_team_id` e `away_team_id` como `"0"` e preenche labels como `"Runner-up Group A"` ou `"Winner Match 73"`. O modelo atual suporta esse caso com `homeTeamLabel` e `awayTeamLabel`.

## Timeline de Eventos

A timeline representa acontecimentos dentro da partida, como:

- `GOAL`
- `YELLOW_CARD`
- `RED_CARD`
- `SUBSTITUTION`
- `PENALTY`
- `VAR_REVIEW`
- `STATUS_CHANGE`

Cada evento pode ter minuto, acréscimo, jogador, seleção relacionada, descrição e horário técnico de ocorrência.

## Endpoints Disponíveis

### Criar partida

```http
POST /matches
```

### Listar partidas

```http
GET /matches
```

Também aceita filtro por status:

```http
GET /matches?status=LIVE
```

### Buscar partida por ID

```http
GET /matches/{id}
```

### Alterar status da partida

```http
PATCH /matches/{id}/status
```

Exemplo de body:

```json
{
  "status": "FINISHED"
}
```

### Adicionar evento na timeline

```http
POST /matches/{id}/timeline-events
```

Exemplo de body:

```json
{
  "type": "GOAL",
  "minute": 12,
  "player": "L. Messi",
  "teamId": "37",
  "description": "Goal from penalty"
}
```

## Estrutura de Pacotes

```text
br.com.infnet.msmatches
|-- client
|-- config
|-- controller
|-- domain
|   `-- enums
|   `-- model
|-- dto
|-- exception
|-- mapper
|-- repository
`-- service
```

## Configuração

Configurações principais em `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: ms-matches
  mongodb:
    uri: ${MONGODB_URI:mongodb://localhost:27017/ms_matches}

integrations:
  core-data:
    base-url: ${CORE_DATA_BASE_URL:http://localhost:8081}
    validation-enabled: ${CORE_DATA_VALIDATION_ENABLED:false}
```

Variáveis de ambiente:

- `MONGODB_URI`: URI de conexão com MongoDB.
- `CORE_DATA_BASE_URL`: URL base do futuro `ms-core-data`.
- `CORE_DATA_VALIDATION_ENABLED`: liga ou desliga validação contra o `ms-core-data`.

Por padrão, a validação com o `ms-core-data` fica desligada porque esse microserviço ainda não existe.

## Integração com ms-core-data

O projeto está preparado para validar referências externas quando o `ms-core-data` existir.

Endpoints assumidos atualmente:

```http
GET /teams/{teamId}
GET /stadiums/{stadiumId}
```

Esses endpoints ainda são provisórios. Quando o `ms-core-data` for implementado, o contrato pode ser ajustado no `CoreDataClient`.

## O Que Ainda Falta

Ainda não faz parte desta versão inicial:

- Ingestão automática da API externa `worldcup2026`.
- Worker com `@Scheduled` para sincronizar jogos.
- Kafka para publicar eventos como `MatchStatusChangedEvent`.
- Redis para cache ou baixa latência em placar ao vivo.
- Autenticação/JWT.
- Testes de integração com MongoDB.
- Contrato real com o `ms-core-data`.
- Escalações e estatísticas detalhadas ao vivo.
- Docker Compose com MongoDB e demais dependências. (Problema a resolver)

## Decisões de Escopo

Redis não foi incluído nesta primeira versão porque o MongoDB já atende o escopo atual de persistir partidas, status, placar e timeline. Redis pode ser avaliado depois se houver necessidade de cache de partidas ao vivo, baixa latência ou controle de concorrência.

Kafka também ficou fora do escopo inicial.

## Como Executar

Com MongoDB disponível em `localhost:27017`:

```powershell
$env:MONGODB_URI='mongodb://localhost:27017/ms_matches'
.\mvnw.cmd spring-boot:run
```

Ou usando o valor padrão do `application.yaml`, basta iniciar a aplicação normalmente.

## Status

Esta versão entrega a base de domínio e API REST do `ms-matches`. Ela está pronta para evoluir para integrações externas, mas ainda não implementa mensageria, ingestão automática ou validação real com outro microserviço.
