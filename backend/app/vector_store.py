import os
import pickle
from typing import List, Dict

import numpy as np
import faiss
from sentence_transformers import SentenceTransformer

from app.config import settings

_embedder: SentenceTransformer | None = None
_index: faiss.Index | None = None
_metadata: List[Dict] = []


def get_embedder() -> SentenceTransformer:
    global _embedder
    if _embedder is None:
        _embedder = SentenceTransformer(settings.EMBEDDING_MODEL)
    return _embedder


def _index_path() -> str:
    return os.path.join(settings.INDEX_DIR, "faiss.index")


def _meta_path() -> str:
    return os.path.join(settings.INDEX_DIR, "metadata.pkl")


def load_index() -> None:
    """Load FAISS index + metadata from disk if present."""
    global _index, _metadata
    if os.path.exists(_index_path()) and os.path.exists(_meta_path()):
        _index = faiss.read_index(_index_path())
        with open(_meta_path(), "rb") as f:
            _metadata = pickle.load(f)


def save_index() -> None:
    """Persist FAISS index + metadata to disk."""
    if _index is not None:
        faiss.write_index(_index, _index_path())
        with open(_meta_path(), "wb") as f:
            pickle.dump(_metadata, f)


def add_chunks(chunks: List[Dict]) -> int:
    """Embed chunks and add them to the FAISS index."""
    global _index, _metadata
    if not chunks:
        return 0

    embedder = get_embedder()
    texts = [c["text"] for c in chunks]
    embeddings = embedder.encode(
        texts, normalize_embeddings=True, show_progress_bar=False
    )
    embeddings = np.asarray(embeddings, dtype="float32")

    if _index is None:
        _index = faiss.IndexFlatIP(embeddings.shape[1])

    _index.add(embeddings)
    _metadata.extend(chunks)
    save_index()
    return len(chunks)


def search(query: str, top_k: int = 4) -> List[Dict]:
    """Return top-k most similar chunks to the query."""
    global _index, _metadata
    if _index is None or _index.ntotal == 0:
        return []

    embedder = get_embedder()
    q_vec = embedder.encode([query], normalize_embeddings=True)
    q_vec = np.asarray(q_vec, dtype="float32")

    k = min(top_k, _index.ntotal)
    scores, indices = _index.search(q_vec, k)

    results: List[Dict] = []
    for score, idx in zip(scores[0], indices[0]):
        if idx == -1 or idx >= len(_metadata):
            continue
        meta = _metadata[idx]
        results.append({
            "text": meta["text"],
            "source": meta["source"],
            "page": meta.get("page"),
            "score": float(score),
        })
    return results


def total_documents() -> int:
    return len(_metadata)
