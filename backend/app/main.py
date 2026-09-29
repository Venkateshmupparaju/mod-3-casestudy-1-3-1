import os
import shutil

from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.document_loader import load_document, chunk_documents
from app.rag_engine import answer_question
from app.schemas import (
    HealthResponse,
    QueryRequest,
    QueryResponse,
    UploadResponse,
)
from app.utils import safe_filename
from app.vector_store import add_chunks, load_index, total_documents

app = FastAPI(
    title="Document QA RAG API",
    version="1.0.0",
    description="RAG-based document question answering service.",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def _startup() -> None:
    load_index()


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(status="ok", documents_indexed=total_documents())


@app.post("/upload", response_model=UploadResponse)
async def upload_document(file: UploadFile = File(...)) -> UploadResponse:
    allowed = (".pdf", ".docx", ".txt", ".md")
    name = safe_filename(file.filename or "document")
    ext = os.path.splitext(name)[1].lower()

    if ext not in allowed:
        raise HTTPException(400, f"Unsupported file type: {ext}")

    save_path = os.path.join(settings.UPLOAD_DIR, name)
    with open(save_path, "wb") as f:
        shutil.copyfileobj(file.file, f)

    try:
        raw_docs = load_document(save_path)
        chunks = chunk_documents(raw_docs)
        n = add_chunks(chunks)
    except Exception as e:
        raise HTTPException(500, f"Failed to process document: {e}")

    return UploadResponse(
        filename=name,
        chunks_indexed=n,
        message="Document indexed successfully.",
    )


@app.post("/query", response_model=QueryResponse)
def query(req: QueryRequest) -> QueryResponse:
    q = req.get_question()
    if not q:
        raise HTTPException(400, "Question cannot be empty.")

    result = answer_question(
        q,
        top_k=req.top_k or settings.TOP_K,
    )
    return QueryResponse(**result)
