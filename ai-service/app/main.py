import json
import os
import re
from typing import Optional

import google.generativeai as genai
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

load_dotenv()
app = FastAPI(title="CineMatch AI Microservice", version="1.0.0")
app.add_middleware(CORSMiddleware, allow_origins=["http://localhost:5173"], allow_credentials=True, allow_methods=["POST", "GET"], allow_headers=["*"])

class PromptRequest(BaseModel):
    prompt: str = Field(min_length=2, max_length=600)

class PromptParseResponse(BaseModel):
    original_prompt: str
    reference_movie: Optional[str] = None
    genres_include: list[str] = []
    genres_exclude: list[str] = []
    genre_ids: list[int] = []
    keywords_include: list[str] = []
    keywords_exclude: list[str] = []
    year_min: Optional[int] = Field(default=None, ge=1888, le=2100)
    year_max: Optional[int] = Field(default=None, ge=1888, le=2100)
    source: str

GENRES = {"action": 28, "adventure": 12, "animation": 16, "comedy": 35, "crime": 80, "documentary": 99, "drama": 18, "family": 10751, "fantasy": 14, "history": 36, "horror": 27, "music": 10402, "mystery": 9648, "romance": 10749, "science fiction": 878, "sci-fi": 878, "thriller": 53, "war": 10752, "western": 37}

def fallback(prompt: str) -> PromptParseResponse:
    lower = prompt.lower()
    selected = [name for name in GENRES if name in lower]
    ids = list(dict.fromkeys(GENRES[name] for name in selected))
    years = [int(y) for y in re.findall(r"\b(?:18|19|20)\d{2}\b", prompt)]
    ref = re.search(r"(?:like|similar to)\s+([\w\s:'-]+?)(?:\s+(?:from|but|with|without|no)\b|$)", prompt, re.I)
    return PromptParseResponse(original_prompt=prompt, reference_movie=ref.group(1).strip() if ref else None, genres_include=list(dict.fromkeys(selected)), genres_exclude=[name for name in GENRES if f"no {name}" in lower or f"without {name}" in lower], genre_ids=ids, year_min=min(years) if years else None, year_max=max(years) if years else None, source="rules-fallback")

@app.get("/health")
def health():
    return {"status": "healthy", "service": "CineMatch AI Microservice", "gemini_configured": bool(os.getenv("GEMINI_API_KEY"))}

@app.post("/api/recommendations/parse-prompt", response_model=PromptParseResponse)
def parse_prompt(request: PromptRequest):
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        return fallback(request.prompt)
    schema = '{"reference_movie":null,"genres_include":[],"genres_exclude":[],"genre_ids":[],"keywords_include":[],"keywords_exclude":[],"year_min":null,"year_max":null}'
    instruction = f"Extract movie recommendation preferences. Return only valid JSON using this schema: {schema}. genre_ids must use TMDB IDs. Never invent a reference movie. Prompt: {request.prompt!r}"
    try:
        genai.configure(api_key=api_key)
        response = genai.GenerativeModel("gemini-1.5-flash").generate_content(instruction, generation_config={"response_mime_type": "application/json", "temperature": 0})
        data = json.loads(response.text)
        data["original_prompt"] = request.prompt
        data["source"] = "gemini"
        return PromptParseResponse.model_validate(data)
    except Exception:
        return fallback(request.prompt)
