package project.rag_basedassistant3_1.models

data class QueryResponse(
    val answer: String,
    val sources: List<String>? = null
)
