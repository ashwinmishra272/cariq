# CARIQ Backend

An AI-powered car recommendation API for the Indian market. Built with Spring Boot and GPT-4o, it takes a user's budget, use case, and preferences — and returns a shortlist of 3 cars with detailed reasoning and trade-off analysis.

---

## What Did You Build and Why?

The backend is a stateless recommendation engine with a thin session-persistence layer. The core idea: instead of building a complex rule engine to match cars, delegate the ranking intelligence to GPT-4o and keep the backend responsible only for data retrieval, prompt construction, and response validation.

The backend:
- Holds a curated catalog of Indian cars (20+ entries across price segments)
- Filters cars to the user's budget before sending anything to the LLM (reduces hallucination risk, keeps prompt token-efficient)
- Constructs a structured prompt with hard constraints from the user's free-text input
- Validates that the LLM's returned car IDs actually exist within the budget-filtered set
- Stores each session (request + raw LLM response) for audit/replay

### What Was Deliberately Cut

- **User accounts / authentication** — sessions are anonymous UUIDs; no login flow
- **Persistent database** — H2 in-memory only; sessions don't survive restarts
- **Streaming responses** — full JSON response is returned at once, no SSE/WebSocket
- **Car image storage** — no media assets, purely data-driven
- **Admin/management endpoints** — no CRUD for the car catalog; the dataset is loaded from `cars.json` at startup
- **Rate limiting** — no throttling on the OpenAI calls; one bad actor could exhaust the API key budget
- **Multi-language support** — English only

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Language | Java 21 | LTS release, virtual threads available, strong typing |
| Framework | Spring Boot 4.0.5 | Production-ready DI, REST, JPA wiring out of the box |
| ORM | Spring Data JPA / Hibernate | Minimal boilerplate for entity persistence |
| Database | H2 (in-memory) | Zero-config, fast startup, acceptable for a demo |
| HTTP Client | Spring RestClient | Native Spring 6 client, no extra dependency |
| LLM | OpenAI GPT-4o via REST | Best instruction-following for structured JSON output |
| Build | Gradle | Concise DSL compared to Maven XML |
| Utilities | Lombok | Eliminates getter/setter/constructor boilerplate |

**Why GPT-4o over a rule engine?**
Car recommendations involve subjective trade-offs ("good mileage vs. safety rating vs. value for money"). A rule engine would require explicit weight-tuning per use case. GPT-4o handles the multi-criteria reasoning naturally and explains its picks in plain English — which is exactly what users need to trust the recommendation.

---

## Project Structure

```
cariq-backend/
├── src/main/java/dev/cariq/
│   ├── car/                  # Car entity, repository, service, controller
│   ├── recommendation/       # Core recommendation logic, OpenAI client, DTOs
│   ├── session/              # Session entity and repository
│   ├── config/               # CORS configuration
│   └── common/               # Global exception handler, ApiError DTO
├── src/main/resources/
│   ├── application.yml       # Production config (env-var driven)
│   ├── application-dev.yml   # Dev overrides (H2 console, SQL logging)
│   └── cars.json             # Seed data for 20+ Indian cars
└── build.gradle
```

---

## API Endpoints

### `POST /api/recommend`

Main recommendation endpoint. Takes user quiz answers, runs them through GPT-4o, and returns 3 ranked cars.

**Request**
```json
{
  "budget": "5-10",
  "use": "City Commute",
  "priorities": ["Good Mileage", "Automatic"],
  "extra": "Must have at least 5 stars NCAP safety rating"
}
```

**Response**
```json
{
  "sessionId": "uuid",
  "shortlist": [ { "id": 4, "make": "Maruti", "model": "Baleno", ... } ],
  "reasoning": { "4": "Best mileage in segment with Automatic option..." },
  "tradeoff": "All three cars are strong in fuel efficiency..."
}
```

### `GET /api/cars`
Returns all cars in the catalog.

### `GET /api/cars/filter?maxPrice=&fuelType=&seating=`
Filters catalog by optional query parameters.

---

## How the Recommendation Works

1. Parse budget string (e.g., `"5-10"`) → extract min/max in lakhs → convert to INR
2. Filter car catalog to those within the budget range
3. Build a GPT-4o prompt that includes:
   - Budget, use case, and priorities
   - The `extra` field verbatim, marked as **hard constraints**
   - Full specs of all budget-eligible cars as JSON
   - Instruction to return exactly 3 car IDs + per-car reasoning + overall trade-off paragraph
4. Call OpenAI `chat/completions`, strip any markdown fences from the response
5. Validate returned IDs are within the budget-filtered set (warn and continue if not)
6. Persist the session and return the structured response

---

## Running Locally

**Prerequisites:** Java 21, `OPENAI_API_KEY` environment variable set

```bash
cd cariq-backend
OPENAI_API_KEY=sk-... ./gradlew bootRun --args='--spring.profiles.active=dev'
```

H2 console available at `http://localhost:8080/h2-console` (dev profile only).

---

## What AI Tools Helped With

- **Prompt engineering** — iterating on the system/user prompt structure to get reliable JSON output without markdown fences was done with AI assistance; getting the "hard constraint" phrasing right took several rounds
- **Boilerplate generation** — entity classes, DTOs, repository interfaces; AI generated these quickly from a schema description
- **Data seeding** — `cars.json` with realistic prices, mileage, ratings, and use cases for 20+ Indian cars was AI-generated and manually verified

## Where AI Got in the Way

- **Hallucinated Spring Boot 4 APIs** — AI occasionally referenced APIs from Spring Boot 3 that changed or moved in Boot 4; had to cross-check against actual docs
- **Over-engineered suggestions** — AI proposals often added unnecessary abstraction layers (e.g., a separate prompt-builder service); kept manually trimming these back to simpler solutions
- **Incorrect Gradle DSL** — generated `build.gradle` snippets sometimes mixed Groovy and Kotlin DSL syntax

---

## If I Had 4 More Hours

1. **Persistent database** — swap H2 for PostgreSQL with a proper migration; sessions currently vanish on restart
2. **Streaming response** — pipe the OpenAI response as SSE so the UI can show tokens appearing live instead of a spinner
3. **Car catalog admin API** — simple authenticated endpoints to add/edit cars without touching `cars.json`
4. **Rate limiting** — token-bucket per IP on `/api/recommend` to protect the OpenAI budget
5. **Caching** — cache GPT responses for identical quiz answers to reduce latency and cost
