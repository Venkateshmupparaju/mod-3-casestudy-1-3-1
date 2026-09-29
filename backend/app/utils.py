import os
import re


def safe_filename(name: str) -> str:
    """Sanitize a filename to prevent path traversal."""
    name = os.path.basename(name)
    name = re.sub(r"[^A-Za-z0-9._-]", "_", name)
    return name or "document"


def truncate(text: str, max_len: int = 500) -> str:
    """Trim text for previews / sources."""
    if len(text) <= max_len:
        return text
    return text[:max_len].rsplit(" ", 1)[0] + "…"
