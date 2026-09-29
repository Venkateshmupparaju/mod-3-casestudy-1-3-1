from typing import Dict, List

from openai import OpenAI

from app.config import settings
from app.utils import truncate
from app.vector_store import search

_client: OpenAI | None = None

SYSTEM_PROMPT = """You are a helpful Document Question Answering assistant.
Answer the user's question using ONLY the provided context.
If the answer cannot be found in the context, say:
"I could not find that in the provided documents."
Always cite the source file name when possible."""


def get_client() -> OpenAI:
    global _client
    if _client is None:
        if not settings.OPENAI_API_KEY:
            raise RuntimeError(
                "OPENAI_API_KEY is not set. Add it to backend/.env"
            )
        _client = OpenAI(api_key=settings.OPENAI_API_KEY)
    return _client


def _build_context(chunks: List[Dict]) -> str:
    parts = []
    for i, c in enumerate(chunks, 1):
        page = f" (page {c['page']})" if c.get("page") else ""
        parts.append(f"[{i}] Source: {c['source']}{page}\n{c['text']}")
    return "\n\n".join(parts)


def answer_question(question: str, top_k: int = 4) -> Dict:
    chunks = search(question, top_k=top_k)

    if not chunks:
        return {
            "answer": "No documents have been indexed yet. "
                      "Please upload a document first.",
            "sources": [],
        }

    context = _build_context(chunks)
    user_prompt = (
        f"Context:\n{context}\n\n"
        f"Question: {question}\n\n"
        f"Answer:"
    )

    client = get_client()
    response = client.chat.completions.create(
        model=settings.LLM_MODEL,
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user_prompt},
        ],
        temperature=0.2,
    )
    answer = response.choices[0].message.content or ""

    sources = [{
        "content": truncate(c["text"], 400),
        "source": c["source"],
        "page": c.get("page"),
        "score": c.get("score"),
    } for c in chunks]

    return {"answer": answer, "sources": sources}
