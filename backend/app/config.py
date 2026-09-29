import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    """Centralized configuration loaded from .env"""

    OPENAI_API_KEY: str = os.getenv("OPENAI_API_KEY", "")
    LLM_MODEL: str = os.getenv("LLM_MODEL", "gpt-4o-mini")
    EMBEDDING_MODEL: str = os.getenv(
        "EMBEDDING_MODEL",
        "sentence-transformers/all-MiniLM-L6-v2"
    )
    CHUNK_SIZE: int = int(os.getenv("CHUNK_SIZE", 800))
    CHUNK_OVERLAP: int = int(os.getenv("CHUNK_OVERLAP", 100))
    TOP_K: int = int(os.getenv("TOP_K", 4))
    UPLOAD_DIR: str = os.getenv("UPLOAD_DIR", "./data/uploads")
    INDEX_DIR: str = os.getenv("INDEX_DIR", "./data/index")


settings = Settings()

# Ensure required directories exist at import time
os.makedirs(settings.UPLOAD_DIR, exist_ok=True)
os.makedirs(settings.INDEX_DIR, exist_ok=True)
