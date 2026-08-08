# Movie Recommendation Platform

A college MCA group project for finding, rating, and obtaining AI-driven personalized movie suggestions.

This project is built using a decoupled Monorepo structure, allowing three developers to work in parallel.

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
3. Update connection credentials in `/backend/src/main/resources/application.properties` (specifically `spring.datasource.username` and `spring.datasource.password`).

### 2. TMDB API Key Setup
1. Register on [The Movie Database (TMDB)](https://www.themoviedb.org/) and generate an API read access token (v4 auth).
2. Configure this key in `/backend/src/main/resources/application.properties` under `tmdb.api.token`.

### 3. AI Service Setup (LLM API Key)
1. Get a free Google AI Studio API key (Gemini API).
2. Configure this key as an environment variable in `/ai-service/.env` or in `/ai-service/app/config.py`.

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
