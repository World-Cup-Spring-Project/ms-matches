# ms-matches

Microserviço de gestão de partidas da Copa do Mundo 2026 para o Arena Cup. Mantém uma cópia local dos jogos vindos do `ms-core-data`, controla o ciclo de vida operacional da partida (status, timeline) e publica eventos no Kafka para o `ms-engagement` abrir e fechar a votação do Craque do Jogo.

## O que este serviço faz

| Responsabilidade | Descrição |
|------------------|-----------|
| **Réplica local** | Sincroniza jogos do `ms-core-data` para o MongoDB |
| **Operação** | Status da partida, timeline de eventos |
| **Craque do Jogo** | Gera top 3 jogadores com nota mock ao finalizar a partida |
| **Integração** | Publica `MatchStatusChangedEvent` no Kafka |

O `ms-core-data` é a fonte dos dados brutos (times, estádios, jogos, placar). O `ms-matches` adiciona a camada de domínio do produto.

## Arquitetura

```text
worldcup2026 API
       │
       ▼
 ms-core-data (:8081)
       │  GET /games
       ▼
 ms-matches (:8082) ──► MongoDB
       │
       │  match-status-changed-events
       ▼
 ms-engagement
```

## Escopo atual

- Sincronização de jogos com o `ms-core-data` (na subida, periódica e manual)
- Consulta de partidas (lista, filtro por status, busca por ID)
- Atualização de status da partida
- Timeline de eventos dentro da partida
- Geração automática dos 3 candidatos ao Craque do Jogo no `FINISHED`
- Publicação de eventos Kafka para o `ms-engagement`
- Cadastro manual de partida (`POST /matches`) — opcional, para testes

## IDs da partida

Cada partida tem dois identificadores:

| Campo | Exemplo | Uso |
|-------|---------|-----|
| `id` | `6a332f74d0225425e6683127` | ID interno do MongoDB |
| `externalMatchId` | `"1"` … `"104"` | ID da Copa / `ms-core-data` |

`GET /matches/{id}`, `PATCH /matches/{id}/status` e `POST /matches/{id}/timeline-events` aceitam **ambos**.

## Modelo de partida

Alinhado com a API [rezarahiminia/worldcup2026](https://github.com/rezarahiminia/worldcup2026):

- `externalMatchId`, `homeTeamId`, `awayTeamId`, `homeScore`, `awayScore`
- `homeTeamLabel`, `awayTeamLabel` (nomes ou placeholders de mata-mata)
- `stadiumId`, `group`, `matchday`, `type`, `status`, `finished`
- `timelineEvents` (eventos manuais do domínio Arena Cup)

Status disponíveis: `SCHEDULED`, `LIVE`, `HALF_TIME`, `FINISHED`, `POST_MATCH_CLOSED`, `POSTPONED`, `CANCELLED`.

## Timeline de eventos

Tipos suportados: `GOAL`, `YELLOW_CARD`, `RED_CARD`, `SUBSTITUTION`, `PENALTY`, `VAR_REVIEW`, `STATUS_CHANGE`.

Cada evento pode ter minuto, acréscimo, jogador, `teamId`, descrição e `occurredAt`.

## Endpoints

### Sincronizar jogos do core-data

```http
POST /matches/sync
```

Importa ou atualiza todos os jogos (upsert por `externalMatchId`).

```http
POST /matches/sync/{externalMatchId}
```

Sincroniza um jogo específico (ex.: `POST /matches/sync/1`).

### Listar partidas

```http
GET /matches
GET /matches?status=LIVE
```

### Buscar partida

```http
GET /matches/{id}
```

Aceita o ID do MongoDB ou o `externalMatchId` da Copa (ex.: `GET /matches/1`).

### Alterar status

```http
PATCH /matches/{id}/status
Content-Type: application/json

{
  "status": "FINISHED",
  "correlationId": "demo-001"
}
```

`correlationId` é opcional (gerado automaticamente se omitido).

**Importante:** `GET /matches/{id}/status` não existe. Para consultar o status, use `GET /matches/{id}`.

### Adicionar evento na timeline

```http
POST /matches/{id}/timeline-events
Content-Type: application/json

{
  "type": "GOAL",
  "minute": 67,
  "player": "L. Messi",
  "teamId": "1",
  "description": "Goal from open play"
}
```

### Criar partida manualmente (opcional)

```http
POST /matches
```

Útil para testes. No fluxo normal, as partidas vêm do sync com o `ms-core-data`.

## Sincronização com ms-core-data

### Como funciona

1. Na **subida** da aplicação (`sync-on-startup: true`), importa os jogos
2. A cada **5 minutos** (`sync-enabled: true`), atualiza placar e dados
3. **Manual** via `POST /matches/sync` quando necessário

O sync faz **upsert** por `externalMatchId`: não duplica jogos em execuções repetidas.

### O que o sync atualiza

- Placar, times, grupo, estádio, data, `finished`
- Status derivado (`SCHEDULED` / `LIVE` / `FINISHED`)

### O que o sync preserva

- `timelineEvents` (eventos manuais)
- `POST_MATCH_CLOSED` (não sobrescreve após fechar a votação)

### Endpoints consumidos no core-data

```http
GET /games
GET /games/{gameId}
GET /teams/{teamId}      (só com validation-enabled)
GET /stadiums/{stadiumId} (só com validation-enabled)
```

## Craque do Jogo (notas mock)

Como a API não retorna jogadores, o serviço usa o arquivo `src/main/resources/data/worldcup.squads.json` como mock de elencos.

Ao marcar `FINISHED`:

1. Busca jogadores dos dois times pelo `homeTeamLabel` e `awayTeamLabel`
2. Sorteia nota entre **6.0** e **10.0** para cada jogador
3. Seleciona os **3 maiores**
4. Publica no evento Kafka

**Limitação:** só funciona com nomes reais de seleção (ex.: Mexico vs South Africa). Jogos de mata-mata com placeholder (`"Runner-up Group A"`) não têm elenco no mock.

## Integração Kafka (ms-engagement)

O `ms-matches` publica no tópico `match-status-changed-events` **sempre** que o status muda via `PATCH /matches/{id}/status`.

**Chave da mensagem:** `matchId` (ID interno do MongoDB).

### Formato do evento

```json
{
  "matchId": "6a332f74d0225425e6683127",
  "status": "FINISHED",
  "correlationId": "demo-001",
  "occurredAt": "2026-06-18T12:00:00.123Z",
  "candidates": [
    { "playerName": "Matěj Kovář", "matchRating": 9.1 },
    { "playerName": "David Zima", "matchRating": 8.7 },
    { "playerName": "Tomáš Holeš", "matchRating": 8.3 }
  ]
}
```

O campo `candidates` só aparece no status `FINISHED`. Nos demais status, é omitido.

### Fluxo típico com o engagement

```text
PATCH status: LIVE              → engagement abre janela (se configurado)
PATCH status: FINISHED          → engagement recebe 3 candidatos
PATCH status: POST_MATCH_CLOSED → engagement fecha votação
```

## Configuração

Arquivo principal: `src/main/resources/application.yaml`

```yaml
server:
  port: 8082

spring:
  mongodb:
    uri: ${MONGODB_URI:mongodb://localhost:27017/ms_matches}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}

integrations:
  core-data:
    base-url: ${CORE_DATA_BASE_URL:http://localhost:8081}
    validation-enabled: ${CORE_DATA_VALIDATION_ENABLED:false}
    sync-enabled: ${CORE_DATA_SYNC_ENABLED:true}
    sync-on-startup: ${CORE_DATA_SYNC_ON_STARTUP:true}
    sync-interval-ms: ${CORE_DATA_SYNC_INTERVAL_MS:300000}

arenacup:
  kafka:
    topics:
      match-status-changed: match-status-changed-events
```

Profile Docker: `src/main/resources/application-docker.yaml` (hostnames de containers).

### Variáveis de ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `MONGODB_URI` | Conexão MongoDB | `mongodb://localhost:27017/ms_matches` |
| `KAFKA_BOOTSTRAP_SERVERS` | Broker Kafka | `localhost:29092` |
| `CORE_DATA_BASE_URL` | URL do ms-core-data | `http://localhost:8081` |
| `CORE_DATA_VALIDATION_ENABLED` | Valida times/estádios na criação manual | `false` |
| `CORE_DATA_SYNC_ENABLED` | Sync periódico | `true` |
| `CORE_DATA_SYNC_ON_STARTUP` | Sync na subida | `true` |
| `CORE_DATA_SYNC_INTERVAL_MS` | Intervalo do sync (ms) | `300000` |
| `EUREKA_CLIENT_ENABLED` | Registro no Eureka | `true` |

**Recomendação:** mantenha `validation-enabled: false` com sync ativo. A validação só faz sentido no `POST /matches` manual.

## Como executar

### Pré-requisitos

- Java 21
- MongoDB
- Kafka
- `ms-core-data` rodando

### Local

```powershell
$env:MONGODB_URI='mongodb://localhost:27017/ms_matches'
$env:KAFKA_BOOTSTRAP_SERVERS='localhost:29092'
$env:CORE_DATA_BASE_URL='http://localhost:8081'
$env:EUREKA_CLIENT_ENABLED='false'
.\mvnw.cmd spring-boot:run
```

Na subida, o sync importa as partidas automaticamente. Depois:

```powershell
curl http://localhost:8082/matches/1
```

### Docker

```powershell
docker build -f docker/Dockerfile -t ms-matches .
docker run -p 8082:8082 -e SPRING_PROFILES_ACTIVE=docker ms-matches
```

## Teste E2E com ms-engagement

1. Suba Kafka, MongoDB, `ms-core-data`, `ms-matches` e `ms-engagement` no **mesmo broker Kafka**
2. Confirme que o engagement consome `match-status-changed-events` e lê `playerName` (não `playerId`)
3. Dispare os status:

```powershell
curl -X PATCH http://localhost:8082/matches/1/status `
  -H "Content-Type: application/json" `
  -d '{"status":"LIVE","correlationId":"demo-001"}'

curl -X PATCH http://localhost:8082/matches/1/status `
  -H "Content-Type: application/json" `
  -d '{"status":"FINISHED","correlationId":"demo-001"}'

curl -X PATCH http://localhost:8082/matches/1/status `
  -H "Content-Type: application/json" `
  -d '{"status":"POST_MATCH_CLOSED","correlationId":"demo-001"}'
```

4. Verifique os logs do `ms-matches` (`Publishing MatchStatusChanged`) e do `ms-engagement`

## Estrutura de pacotes

```text
br.com.infnet.msmatches
|-- client/          # CoreDataClient + DTOs do core-data
|-- config/
|-- controller/
|-- domain/
|   |-- enums/
|   `-- model/       # Match, TimelineEvent, SquadPlayer
|-- dto/
|   |-- request/
|   |-- response/
|   `-- squad/       # DTOs do JSON de elencos
|-- exception/
|-- infra/kafka/     # Publisher + eventos
|-- mapper/
|-- repository/
`-- service/         # Match, Sync, Rating, Squad
```