from pydantic import BaseModel, Field
from typing import List, Optional


class QueryRequest(BaseModel):
    question: Optional[str] = None
    query: Optional[str] = None
    top_k: Optional[int] = 4

    def get_question(self) -> str:
        q = self.question or self.query or ""
        return q.strip()


class SourceChunk(BaseModel):
    content: str
    source: str
    page: Optional[int] = None
    score: Optional[float] = None


class QueryResponse(BaseModel):
    answer: str
    sources: List[SourceChunk] = []


class UploadResponse(BaseModel):
    filename: str
    chunks_indexed: int
    message: str


class HealthResponse(BaseModel):
    status: str
    documents_indexed: int
