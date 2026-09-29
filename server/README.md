# ThantFlash Server (Java / Spring Boot)

ThantFlash flash card app ရဲ့ **REST API backend** ပါ။
အလုပ်လျှောက်တဲ့အခါ portfolio အဖြစ် ပြဖို့ ရည်ရွယ်ပြီး ဂျပန် IT ကုမ္ပဏီတွေ (SIer / Web系) မှာ
တကယ်သုံးနေတဲ့ နည်းပညာတွေနဲ့ ရေးထားပါတယ်။

> 🇯🇵 日本語の紹介文（面接・ES用）→ [`docs/PORTFOLIO_ja.md`](docs/PORTFOLIO_ja.md)
> 📐 設計書（ER図・API一覧・シーケンス図）→ [`docs/DESIGN.md`](docs/DESIGN.md)

## Tech stack

| အပိုင်း | နည်းပညာ |
| --- | --- |
| Language | Java 21 (record, switch expression, text block) |
| Framework | Spring Boot 3.5 (Web, Validation, Data JPA, Security, Actuator) |
| Auth | Spring Security + JWT (HS256), BCrypt password hash |
| DB | H2 (dev/test) / PostgreSQL 16 (prod), **Flyway** migration |
| API docs | springdoc-openapi (Swagger UI) |
| Test | JUnit 5, AssertJ, MockMvc (unit + integration) |
| Infra | Docker, docker compose, GitHub Actions CI |

## Run (Java 21 လိုတယ်)

```bash
cd server
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

- Swagger UI → http://localhost:8080/swagger-ui.html
- H2 console → http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:thantflash`, user `sa`)

DB က memory ထဲမှာပဲ ရှိလို့ app ပိတ်ရင် data ပျောက်ပါတယ်။ တကယ့် DB နဲ့ run ချင်ရင်:

```bash
docker compose up --build       # PostgreSQL + app
```

Test run:

```bash
./mvnw verify
```

## Swagger UI နဲ့ စမ်းကြည့်နည်း

1. `POST /api/auth/register` → `{"username":"thantzin","password":"password123"}` → `token` ကို copy
2. ညာဘက်အပေါ် **Authorize 🔒** နှိပ် → token ထည့်
3. `POST /api/decks` → `{"name":"JLPT N2"}`
4. `POST /api/decks/{deckId}/cards` → `{"front":"締切","back":"しめきり — deadline"}`
5. `GET /api/study/due` → လေ့လာရမယ့် ကတ်တွေ
6. `POST /api/cards/{cardId}/review` → `{"grade":"GOOD"}` (AGAIN / HARD / GOOD / EASY)
7. `GET /api/stats` → ဒီနေ့ လေ့လာပြီးတဲ့ အရေအတွက်၊ streak၊ accuracy

## API

| Method | Path | ဘာလုပ်လဲ |
| --- | --- | --- |
| POST | `/api/auth/register` | အကောင့်ဖွင့် → JWT |
| POST | `/api/auth/login` | Login → JWT |
| GET | `/api/decks` | Deck စာရင်း (ကတ်အရေအတွက်၊ due အရေအတွက်ပါ) |
| POST | `/api/decks` | Deck အသစ် |
| PUT | `/api/decks/{id}` | Deck နာမည်ပြောင်း |
| DELETE | `/api/decks/{id}` | Deck ဖျက် (ကတ်တွေပါ ပျက်) |
| GET | `/api/decks/{id}/cards?q=` | ကတ်စာရင်း / ရှာ |
| POST | `/api/decks/{id}/cards` | ကတ်အသစ် |
| PUT | `/api/cards/{id}` | ကတ်ပြင် (deck ပြောင်းလို့ရ) |
| DELETE | `/api/cards/{id}` | ကတ်ဖျက် |
| POST | `/api/import/tsv?deck=` | Anki `.tsv` import (body = text/plain) |
| GET | `/api/study/due?deckId=&limit=` | ယခု လေ့လာရမယ့် ကတ်များ |
| POST | `/api/cards/{id}/review` | အဖြေအမှတ်ပေး → SM-2 နဲ့ နောက်ရက် တွက် |
| GET | `/api/stats?zone=Asia/Tokyo` | စာရင်းအင်း |

Error တွေကို RFC 9457 `application/problem+json` ပုံစံနဲ့ ပြန်ပေးတယ်:
`{"status":404,"title":"Not Found","detail":"Deck 7 not found"}`

## Code ဖွဲ့စည်းပုံ (package-by-feature)

```
src/main/java/com/thantzin/thantflash/
├── auth/     register / login / JWT ထုတ်
├── user/     User entity
├── deck/     Deck CRUD
├── card/     Card CRUD, TSV import
├── review/   SM-2 algorithm, review, stats
├── config/   Security, CORS, Swagger
└── common/   Exception → HTTP error
```

တစ် feature ချင်းစီမှာ **Controller → Service → Repository → Entity** အလွှာ (layer) ခွဲထားတယ်:

- **Controller** — HTTP request/response ပဲ ကိုင်တယ်၊ logic မရှိ
- **Service** — business logic, `@Transactional`
- **Repository** — Spring Data JPA, DB query
- **DTO (record)** — API ရဲ့ input/output; Entity ကို အပြင်တိုက်ရိုက် မထုတ်

## ဖတ်ရမယ့် အစဉ် (လေ့လာသူအတွက်)

1. `review/Sm2Scheduler.java` + `Sm2SchedulerTest.java` — Spring မပါတဲ့ pure Java logic နဲ့ unit test
2. `deck/` တစ်ခုလုံး — CRUD ရဲ့ ပုံစံ (Controller → Service → Repository)
3. `DeckRepository.findSummaries` — JPQL `group by` + DTO projection
4. `config/SecurityConfig.java` + `auth/AuthService.java` — JWT ဘယ်လိုထုတ်၊ ဘယ်လိုစစ်
5. `ApiIntegrationTest.java` — app တစ်ခုလုံးကို MockMvc နဲ့ test

## Interview မှာ ပြောလို့ရတဲ့ အချက်တွေ

- **User isolation** — တခြားလူရဲ့ deck/card ကို ယူရင် 403 မဟုတ်ဘဲ 404 ပြန်တယ် (ရှိမှန်းတောင် မသိစေ)။ Test ရေးထားတယ်။
- **Clock injection** — `Instant.now()` ကို တိုက်ရိုက်မခေါ်ဘဲ `Clock` bean ကို inject လုပ်ထားလို့ အချိန်ပေါ်မူတည်တဲ့ logic ကို test လုပ်ရလွယ်တယ်။
- **N+1 ရှောင်** — `join fetch` နဲ့ deck count ကို query တစ်ခုတည်းနဲ့ ယူတယ်။
- **Schema ကို Flyway က ပိုင်** — `ddl-auto: validate` ဆိုတော့ entity နဲ့ table မကိုက်ရင် app မတက်ဘူး။
- **Stateless** — session မသုံး၊ JWT ပဲ → server အများကြီး scale လုပ်လို့ရ။

## နောက်ထပ် လုပ်ကြည့်ရန် (Roadmap)

- [ ] Web app (`app.js`) ကနေ ဒီ API ကို ခေါ်ပြီး ဖုန်း/ကွန်ပျူတာ sync
- [ ] Refresh token / logout
- [ ] Reminder API
- [ ] Testcontainers နဲ့ PostgreSQL test
- [ ] Render / Railway / AWS ပေါ် deploy
