# Pastebin Lite – Spring Boot

A minimal Pastebin-like backend application built using **Spring Boot** and **Redis**.

---

##  Features

- Create a paste via REST API or UI
- View a paste using a shared link
- Optional time-based expiration (TTL)
- Optional view-limit expiration
- Automatic cleanup of expired pastes
- Redis-based persistence
- Health check endpoint

---

##  Tech Stack

- Java 17
- Spring Boot
- Redis
- Maven

---

## Persistence Choice

Redis is used as the primary datastore because:
- It provides native TTL support for expiring pastes
- It offers fast key-value access
- It avoids schema and migration overhead for temporary data

Pastes are stored as JSON strings using the key format:


---

##  Notable Design Decisions

- Used `StringRedisTemplate` with manual JSON serialization to avoid
  Redis/Jackson serializer compatibility and deprecation issues
- Pastes are deleted once:
    - Their TTL expires
    - The maximum view count is exhausted
- Clear and consistent error responses:
    - `400` for invalid input
    - `404` for expired or missing pastes
- Minimal HTML UI provided to demonstrate end-to-end flow

---

##  Running the Application

### Prerequisites
- Java 17+
- Redis running on `localhost:6379`

### Run
```bash
mvn spring-boot:run

 API Endpoints
Health Check
GET /api/healthz

Create Paste
POST /api/pastes


Body:

{
  "content": "Hello world",
  "ttl_seconds": 60,
  "max_views": 3
}

View Paste
GET /api/pastes/{id}

 UI

Create paste: http://localhost:8080/

View paste: http://localhost:8080/p/{id}