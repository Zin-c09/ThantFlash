# ThantFlash API — Spring Boot backend

REST API behind the [ThantFlash](../README.md) flash-card app: accounts, decks,
cards with **SM-2 spaced repetition**, study statistics, Anki TSV import and
scheduled reminders.

**Stack:** Java 21 · Spring Boot 3.5 · Spring Security (JWT resource server) ·
Spring Data JPA / Hibernate · PostgreSQL · Flyway · springdoc OpenAPI ·
JUnit 5 / MockMvc · Docker · GitHub Actions

## Run it

```bash
# Everything in Docker (Postgres + API)
docker compose up --build

# Or: Postgres in Docker, API from your IDE / Maven
docker compose up -d db
mvn spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

Tests use in-memory H2 (PostgreSQL mode), so no database is needed:

```bash
mvn test
```

## Try it with curl

```bash
TOKEN=$(curl -s localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"me@example.com","password":"password123","displayName":"Thant Zin"}' | jq -r .accessToken)

curl -s localhost:8080/api/decks -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"name":"JLPT N2"}'
curl -s localhost:8080/api/import/tsv -H "Authorization: Bearer $TOKEN" -F file=@cards.tsv
curl -s localhost:8080/api/study/due -H "Authorization: Bearer $TOKEN"
curl -s localhost:8080/api/cards/1/review -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"grade":"GOOD"}'
curl -s localhost:8080/api/stats -H "Authorization: Bearer $TOKEN"
```

## API

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register`, `/api/auth/login` | Get a JWT |
| GET | `/api/auth/me` | Current user |
| GET/POST | `/api/decks` | List decks (with card + due counts) / create |
| PUT/DELETE | `/api/decks/{id}` | Rename / delete (cascades to cards) |
| GET/POST | `/api/decks/{id}/cards?q=&page=&size=` | Search + paginate / add card |
| GET/PUT/DELETE | `/api/cards/{id}` | Read / edit or move / delete |
| GET | `/api/study/due?deckId=&limit=` | Study queue, oldest due first |
| POST | `/api/cards/{id}/review` | Grade `AGAIN`/`HARD`/`GOOD`/`EASY`, reschedule |
| POST | `/api/import/tsv` | Anki-style `Front<TAB>Back<TAB>Deck` upload |
| GET | `/api/stats` | Reviews today, streak, 30-day retention and history |
| GET/POST | `/api/reminders` | List / create (`NONE`, `DAILY`, `WEEKLY`) |
| PUT/DELETE | `/api/reminders/{id}` | Edit / delete |
| POST | `/api/reminders/{id}/toggle` | Mark done / undone |

Errors are RFC 9457 `application/problem+json`; validation errors include a
per-field `errors` map.

## Design notes

```
controller  →  service (@Transactional)  →  repository (Spring Data JPA)  →  PostgreSQL
                    ↘ Sm2Scheduler / TsvParser / Repeat (pure, unit-tested)
```

- **Package by feature** (`auth`, `deck`, `card`, `review`, `reminder`) rather than by layer.
- **Stateless JWT** using Spring Security's built-in OAuth2 resource server and
  Nimbus encoder — no third-party JWT library. Passwords are BCrypt-hashed.
- **Multi-tenancy by owner**: every query is scoped to the caller's user id, and
  another user's resources return `404`, not `403`, so ids can't be probed.
- **SM-2 scheduling is a pure function** (`Sm2Scheduler.next(state, grade, now)`),
  identical to the browser app's rules, so it's fully unit-tested without Spring.
- **Optimistic locking** (`@Version`) on cards so two tabs grading the same card
  can't silently overwrite each other.
- **Append-only review log** feeds stats; days are bucketed in the user's own
  time zone (default `Asia/Yangon`), and streaks survive until the user studies today.
- **No N+1**: the deck list gets card and due counts in one aggregate JPQL query;
  due queues use `join fetch`.
- **Schema owned by Flyway**, Hibernate only validates (`ddl-auto: validate`).
- **Injectable `Clock`** so time-based logic is deterministic in tests.
- **Reminder job** polls every 30 s in batches; repeating reminders roll forward
  in local wall-clock time (DST-safe). Delivery goes through a `ReminderNotifier`
  interface (logging by default — plug in email / Web Push / Telegram).

## Roadmap (good next steps)

- Testcontainers-based tests against real PostgreSQL
- Refresh tokens and rate limiting on `/api/auth/login`
- ShedLock so the reminder job is safe with several instances
- Connect the web app (`../app.js`) to this API with offline sync
- Email / Web Push reminder delivery
