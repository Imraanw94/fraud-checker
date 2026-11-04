# Fraud Detection Service

## Overview

The Fraud Detection Service is a Java 21 and Spring Boot REST application that accepts financial transactions, evaluates them against fraud rules, stores the transactions and evaluation results in PostgreSQL, and exposes APIs to retrieve those results. Flyway applies database migrations automatically when the application starts.

## Architecture

The project follows BCE (Boundary-Control-Entity) architecture:

- **Boundary** (`boundary`): REST controllers and HTTP request/response handling. Controllers delegate work and do not contain business logic.
- **Control** (`control`): application workflow and business logic, including transaction processing and fraud evaluation.
- **Entity** (`entity`): domain models, JPA entities and repositories, request/response records, and enums.

## Requirements

- Java 21
- Docker with the Compose plugin (for the database or the full Docker environment)

## Build and run locally

Build the project and run its tests with the Maven Wrapper:

```bash
./mvnw clean package
```

Start PostgreSQL using the Compose database service:

```bash
docker compose up -d database
```

Configure the local application to connect to the default database.

```bash
export DB_URL=jdbc:postgresql://localhost:5432/fraudcheck
export DB_USERNAME=postgres
export DB_PASSWORD=password
```

Run the application:

```bash
./mvnw spring-boot:run
```

The service listens on http://localhost:8080. Flyway creates and updates the schema on startup. Stop the local database with `docker compose down`; add `-v` to also delete its persisted data.

## Run with Docker Compose

Docker Compose builds the application image, starts PostgreSQL, waits for the database health check, and then starts the application. The defaults are defined in `docker-compose.yml`.

```bash
docker compose up --build -d
```

View application logs:

```bash
docker compose logs -f app
```

Stop the services with `docker compose down`. To also remove the persisted database volume, use `docker compose down -v`.

## Database

The service uses PostgreSQL. Docker Compose provides PostgreSQL 15.3 with a persistent volume; its default database is `fraudcheck`, with the username `postgres` and password `password`. Configure different values with `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` in `.env`. For a locally run application, set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to match the PostgreSQL instance.

Flyway migrations are stored in `src/main/resources/db/migration` and run automatically at application startup. Hibernate validates the schema against the mapped entities.

## API documentation

With the service running, open:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Example API requests and responses

Submit a transaction:

```bash
curl -i -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC-1001",
    "amount": 250.00,
    "currency": "USD",
    "merchant": "Acme Store",
    "country": "US"
  }'
```

Example `201 Created` response:

```json
{
  "id": "6f55d457-ea95-488f-afce-3ce6dd9294a0",
  "transactionId": "8c803a16-9982-44ad-84ac-d075f7b34756",
  "decision": "REVIEW",
  "riskScore": 40,
  "evaluatedAt": "2026-01-15T10:30:00Z"
}
```

The country `US` contributes a risk score of 40, which results in a `REVIEW` decision under the current rules. The identifiers and timestamp above are examples; each submission generates its own values.

Retrieve the result using the returned `transactionId`:

```bash
curl http://localhost:8080/api/transactions/8c803a16-9982-44ad-84ac-d075f7b34756/fraud-result
```

The response has the same shape as the submission response.
