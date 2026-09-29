package project.rag_basedassistant3_1.models

data class QueryRequest(
    val query: String,
    val top_k: Int = 3
)
