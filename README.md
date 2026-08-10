# CineMatch — AI Movie Recommendation Platform

A college MCA group project for finding, rating, and obtaining AI-driven personalized movie suggestions.

This project is built using a decoupled Monorepo structure, allowing three developers to work in parallel.

## Implemented features

- JWT registration, login, and authenticated user profile
- TMDB-backed trending, popular, search, genre, and detail APIs with local caching
- Watchlist, watched history, ratings, and community-review data stored by TMDB ID
- Gemini semantic prompt parser with a reliable no-key rules fallback
- Dual-path recommendations (similar titles + discover filters), excluding watched and poorly rated movies
- React discovery, search, sign-in, profile, and AI recommendation views

Movie cards open a details page where authenticated users can save a title to their watchlist or watched history. The frontend requests all data and TMDB poster images through Spring Boot; it never calls TMDB or the AI service directly.

Docker remains a future deployment option; use the local steps below.

## Repository Structure
- **`/frontend`**: React client SPA (Vite + Tailwind CSS + React Router + Axios)
- **`/backend`**: Spring Boot REST API (Spring Web, Spring Security, JWT, JPA, PostgreSQL, Spring Cache)
- **`/ai-service`**: FastAPI Microservice (Python, Gemini / LLM API, Pydantic)

---

## Getting Started Locally

### 1. Database Setup
1. Install **PostgreSQL** locally.
2. Open your preferred SQL client (e.g. pgAdmin, DBeaver) or terminal and run:
   ```sql
   CREATE DATABASE movierec;
   ```
3. Set `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` if you do not use the defaults.

### 2. TMDB API Key Setup
1. Register on [The Movie Database (TMDB)](https://www.themoviedb.org/) and generate an API read access token (v4 auth).
2. Set it in the `TMDB_API_TOKEN` environment variable.

### 3. AI Service Setup (LLM API Key)
1. Get a free Google AI Studio API key (Gemini API).
2. Copy `ai-service/.env.example` to `ai-service/.env` and provide `GEMINI_API_KEY`. Without it, the service uses a basic rules fallback.

---

## Running the Services

### Start the AI Service (FastAPI)
```bash
cd ai-service
python -m venv .venv
# On Windows:
.venv\Scripts\activate
# On Linux/macOS:
source .venv/bin/activate

pip install -r requirements.txt
uvicorn app.main:app --reload
```
Runs on: `http://localhost:8000`

### Start the Backend (Spring Boot)
1. Import `/backend` as a Maven project into your IDE (IntelliJ IDEA is recommended).
2. Run the `MovieRecommendationApplication` main class, or run via terminal:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
Runs on: `http://localhost:8080`

### Start the Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```
Runs on: `http://localhost:5173`
