package project.rag_basedassistant3_1.models

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val sources: List<String>? = null,
    val timestamp: Long = System.currentTimeMillis()
)
