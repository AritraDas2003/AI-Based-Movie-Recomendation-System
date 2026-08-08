# CineMatch: Functional Features & Implementation Roadmap

This document outlines all the functional features for **CineMatch** (AI-Based Movie Recommendation Platform), broken down into actionable milestones and step-by-step tasks.

---

## 🎯 Milestone 1: Authentication & User Management (Backend & DB)

- [ ] **1.1 User Entity & Repository (PostgreSQL)**
  - Schema for `users` table: `id`, `username`, `email`, `password_hash`, `created_at`.
  - Spring Data JPA `UserRepository` interface.
- [ ] **1.2 Auth DTOs & Password Encryption**
  - DTOs: `RegisterRequest`, `LoginRequest`, `AuthResponse`, `UserDTO`.
  - Password encoding with `BCryptPasswordEncoder`.
- [ ] **1.3 JWT Authentication Filter & Provider**
  - Token generation (`JwtTokenProvider`) with secret key & expiration.
  - Custom `JwtAuthenticationFilter` verifying `Authorization: Bearer <token>` header.
  - Security Config configuring stateless session management and public vs protected endpoints.
- [ ] **1.4 Auth Endpoints (`/api/auth`)**
  - `POST /api/auth/register` — Create new user account.
  - `POST /api/auth/login` — Authenticate and return JWT token.
  - `GET /api/auth/me` — Fetch currently authenticated user profile.

---

## 🎬 Milestone 2: TMDB Service & Backend Caching (Spring Boot)

- [ ] **2.1 TMDB API Integration Configuration**
  - Configure `tmdb.api.token` and `tmdb.api.base-url` in `application.properties`.
  - `RestTemplate` or `WebClient` bean configured with TMDB v4 Bearer token authentication headers.
- [ ] **2.2 Caching Strategy (`CacheConfig.java`)**
  - Configure `ConcurrentMapCacheManager` with caches: `trendingMovies`, `movieDetails`, `genresList`, `searchCache`.
- [ ] **2.3 Movie Proxy Controller & Services (`/api/movies`)**
  - `GET /api/movies/trending` — Cached trending movies list.
  - `GET /api/movies/popular` — Cached popular movies list.
  - `GET /api/movies/genres` — List of official TMDB genres mapped to IDs.
  - `GET /api/movies/{id}` — Full movie details (overview, genres, cast, runtime, release date, backdrop/poster paths).
  - `GET /api/movies/search?query={query}` — Title-based movie search.

---

## 📌 Milestone 3: User Activity & History System (PostgreSQL)

- [ ] **3.1 Watchlist Management**
  - Entity `WatchlistItem` (`id`, `user_id`, `tmdb_movie_id`, `added_at`).
  - `POST /api/watchlist/{tmdbMovieId}` — Add movie to user watchlist.
  - `DELETE /api/watchlist/{tmdbMovieId}` — Remove movie from user watchlist.
  - `GET /api/watchlist` — Retrieve user watchlist (fetches TMDB metadata dynamically for each `tmdb_movie_id`).
- [ ] **3.2 User Movie Ratings & Reviews**
  - Entity `MovieRating` (`id`, `user_id`, `tmdb_movie_id`, `rating` (1.0-5.0), `review_text`, `updated_at`).
  - `POST /api/ratings` — Submit or update movie rating & optional review.
  - `GET /api/ratings/user` — Get all ratings submitted by the current user.
  - `GET /api/ratings/movie/{tmdbMovieId}` — Get community ratings/reviews for a specific movie.
- [ ] **3.3 Viewing History / Watched List**
  - Entity `WatchedHistory` (`id`, `user_id`, `tmdb_movie_id`, `watched_at`).
  - `POST /api/history/{tmdbMovieId}` — Mark movie as watched.
  - `DELETE /api/history/{tmdbMovieId}` — Remove movie from watched history.
  - `GET /api/history` — Get user's complete viewing history.

---

## 🤖 Milestone 4: FastAPI AI Semantic Parser (AI Service)

- [ ] **4.1 LLM / Gemini Setup**
  - Configure Google Gemini API client in `app/config.py`.
  - Pydantic models for structured output: `PromptParseResponse` (`reference_movie`, `genres_include`, `genres_exclude`, `year_min`, `year_max`, `keywords_include`, `keywords_exclude`).
- [ ] **4.2 Semantic Prompt Parsing Endpoint**
  - `POST /api/recommendations/parse-prompt`
  - System prompt engineering to extract structured user preferences accurately from freeform text.
  - Fallback logic for ambiguous or complex queries.

---

## 🔀 Milestone 5: Dual-Path Recommendation Engine (Spring Boot)

- [ ] **5.1 FastAPI Microservice Client**
  - Spring Boot service to send prompt payload to FastAPI `http://localhost:8000/api/recommendations/parse-prompt`.
- [ ] **5.2 Dual-Path Candidate Retrieval**
  - **Path 1 (Similar Movies)**: If `reference_movie` is detected, search TMDB for the movie ID, then call `/movie/{id}/similar`.
  - **Path 2 (Genre & Filter Discover)**: Call TMDB `/discover/movie` with parsed `genres_include`, release date range (`year_min` to `year_max`), and keyword filters.
- [ ] **5.3 PostgreSQL Exclusion & Scoring Algorithm**
  - Fetch user's watched history and low-rated movies from PostgreSQL.
  - Exclude watched/blacklisted movies from the candidate list.
  - Score candidates based on match frequency, vote average, and keyword alignment.
- [ ] **5.4 Recommendation Endpoint (`/api/recommendations`)**
  - `POST /api/recommendations/ai` — Accepts `{ "prompt": "..." }`, runs full recommendation pipeline, and returns enriched movie cards with AI reasoning tags.

---

## 🖥️ Milestone 6: Glassmorphism Frontend UI (React + Tailwind CSS)

- [ ] **6.1 Core Layout & Styling Setup**
  - Define custom glassmorphism styles (`.glass-panel`, `.glass-card`, `.glass-nav`, `.glass-input`) in `index.css`.
  - Responsive Navbar with navigation links, search bar, and Auth status (Login/Register/Logout buttons).
- [ ] **6.2 Authentication UI**
  - Login & Registration modals/pages with error handling and JWT token storage in `localStorage`.
  - Axios interceptor injecting `Authorization: Bearer <token>` header for all API requests.
- [ ] **6.3 Home & Discovery Page**
  - Hero section featuring the AI Conversational Search Bar.
  - Quick-prompt suggestion chips (e.g., *"90s Action Thrillers"*, *"Mind-bending Sci-Fi"*, *"Feel-good RomComs"*).
  - Horizontal scrolling carousels for **Trending Movies** and **Popular Movies**.
- [ ] **6.4 AI Recommendation Results View**
  - Real-time loading/thinking spinner with AI prompt breakdown.
  - Interactive grid of recommended movie cards displaying poster, release year, genre tags, TMDB rating, and AI match explanation.
- [ ] **6.5 Movie Details Modal / Page**
  - High-res backdrop header with movie details (synopsis, director/cast, duration, genres).
  - Quick actions: "Add to Watchlist", "Mark as Watched", "Rate & Review".
  - Community reviews list.
- [ ] **6.6 User Dashboard / Profile Page**
  - Tabbed interface for:
    - 📋 **Watchlist**: Saved movies for later.
    - ✅ **Watched History**: List of previously viewed movies.
    - ⭐ **My Ratings & Reviews**: User's past ratings with edit capabilities.

---

## 🧪 Milestone 7: Integration & End-to-End Testing

- [ ] **7.1 Backend API Verification**
  - Integration testing for Spring Boot endpoints & PostgreSQL operations.
- [ ] **7.2 AI Parser Accuracy Verification**
  - Test natural language prompts against FastAPI microservice with various movie genres and constraints.
- [ ] **7.3 Full Flow E2E Testing**
  - Verify complete user workflow: Auth -> AI Search -> Receive Recommendations -> Add to Watchlist / Mark as Watched -> Verified filter exclusion in subsequent recommendations.
