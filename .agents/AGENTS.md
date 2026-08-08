# CineMatch: AI Movie Recommendation Platform
## Workspace Rules & Architectural Guidelines

This workspace is a Monorepo containing:
- `/frontend` (React + Vite + Tailwind CSS)
- `/backend` (Spring Boot + PostgreSQL + Hibernate)
- `/ai-service` (FastAPI + Python + LLM API)

---

## 1. Core Architectural Constraints
* **No Direct External Calls from React**: React must NEVER call TMDB or the AI Model/FastAPI service directly. All client requests must route through Spring Boot `/api/...` proxies.
* **Single Source of Truth (TMDB ID)**: Do NOT mirror the TMDB movie catalog in PostgreSQL. The database must store only TMDB movie IDs (`tmdb_movie_id` mapped as `BIGINT/Long`) in the ratings, reviews, watchlist, and history tables. Spring Boot will fetch details dynamically from TMDB and merge them on-the-fly.
* **Caching Strategy**: We omit Redis for simplicity. Spring Boot utilizes local in-memory caching (`ConcurrentMapCacheManager`) configured in `CacheConfig.java` to cache TMDB responses (trending, details, genres).
* **AI Service Role**: The FastAPI microservice is purely a semantic query parser. It does not search the database or call TMDB. It parses raw text prompts into a structured JSON preference filter using LLM API schemas.

---

## 2. API Schema Rules
* Spring Boot coordinates recommendation fusion using a **Dual-Path Retrieval** strategy:
  1. Retrieve similar candidates via TMDB `/movie/{id}/similar` using the resolved reference ID.
  2. Retrieve filter candidates via TMDB `/discover/movie` using parsed genres and release years.
  3. Merge candidates, filter out already watched or low-rated movies (based on PostgreSQL data), and score results.
* All endpoints must return standard JSON DTOs matching the structures defined in the API Design documentation.

---

## 3. Tech Stack & Styling Standards
* **Frontend**: React 18, React Router v6, Axios, and TanStack React Query.
* **Aesthetics**: Premium Glassmorphism. Use custom CSS classes (`.glass-panel`, `.glass-card`, `.glass-nav`) defined in `/frontend/src/index.css`.
* **Backend Security**: Stateless JWT authentication filter checking `Authorization: Bearer <token>` in the Security filter chain.
