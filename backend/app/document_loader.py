import os
from typing import List, Dict

from pypdf import PdfReader
from docx import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter

from app.config import settings


def load_pdf(file_path: str) -> List[Dict]:
    reader = PdfReader(file_path)
    docs: List[Dict] = []
    for i, page in enumerate(reader.pages):
        text = page.extract_text() or ""
        if text.strip():
            docs.append({
                "text": text,
                "page": i + 1,
                "source": os.path.basename(file_path),
            })
    return docs


def load_docx(file_path: str) -> List[Dict]:
    doc = Document(file_path)
    text = "\n".join(p.text for p in doc.paragraphs if p.text.strip())
    return [{
        "text": text,
        "page": None,
        "source": os.path.basename(file_path),
    }]


def load_txt(file_path: str) -> List[Dict]:
    with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
        text = f.read()
    return [{
        "text": text,
        "page": None,
        "source": os.path.basename(file_path),
    }]


def load_document(file_path: str) -> List[Dict]:
    """Dispatch to the correct loader based on file extension."""
    ext = os.path.splitext(file_path)[1].lower()
    if ext == ".pdf":
        return load_pdf(file_path)
    if ext == ".docx":
        return load_docx(file_path)
    if ext in (".txt", ".md"):
        return load_txt(file_path)
    raise ValueError(f"Unsupported file type: {ext}")


def chunk_documents(docs: List[Dict]) -> List[Dict]:
    """Split pages/docs into overlapping chunks for embedding."""
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=settings.CHUNK_SIZE,
        chunk_overlap=settings.CHUNK_OVERLAP,
        separators=["\n\n", "\n", ". ", " ", ""],
    )
    chunks: List[Dict] = []
    for d in docs:
        for chunk in splitter.split_text(d["text"]):
            if chunk.strip():
                chunks.append({
                    "text": chunk,
                    "source": d["source"],
                    "page": d.get("page"),
                })
    return chunks
