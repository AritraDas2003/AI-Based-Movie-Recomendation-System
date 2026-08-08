from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="CineMatch AI Microservice",
    description="Natural language parsing microservice for Movie Recommendation Platform",
    version="0.1.0"
)

# CORS Config
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def read_health():
    return {
        "status": "healthy",
        "service": "CineMatch AI Microservice",
        "scaffold": "FastAPI Setup Completed"
    }

@app.post("/api/recommendations/parse-prompt")
def parse_prompt(payload: dict):
    # Temporary mock implementation to confirm route is alive
    prompt = payload.get("prompt", "")
    return {
        "original_prompt": prompt,
        "reference_movie": None,
        "genres_include": [],
        "genres_exclude": [],
        "keywords_include": [],
        "keywords_exclude": [],
        "year_min": None,
        "year_max": None,
        "message": "AI parser scaffold active. Awaiting model key configuration."
    }
