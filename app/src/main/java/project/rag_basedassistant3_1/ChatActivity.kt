package project.rag_basedassistant3_1

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import project.rag_basedassistant3_1.adapter.MessageAdapter
import project.rag_basedassistant3_1.models.ChatMessage
import project.rag_basedassistant3_1.models.QueryRequest

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var etQuery: EditText
    private lateinit var btnSend: Button
    private lateinit var progressBarChat: ProgressBar
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerViewChat = findViewById(R.id.recyclerViewChat)
        etQuery = findViewById(R.id.etQuery)
        btnSend = findViewById(R.id.btnSend)
        progressBarChat = findViewById(R.id.progressBarChat)

        adapter = MessageAdapter()
        recyclerViewChat.layoutManager = LinearLayoutManager(this)
        recyclerViewChat.adapter = adapter

        btnSend.setOnClickListener {
            val queryText = etQuery.text.toString().trim()
            if (queryText.isNotEmpty()) {
                sendQuery(queryText)
            }
        }
    }

    private fun sendQuery(query: String) {
        val userMsg = ChatMessage(text = query, isUser = true)
        adapter.addMessage(userMsg)
        recyclerViewChat.smoothScrollToPosition(adapter.itemCount - 1)
        etQuery.setText("")

        progressBarChat.visibility = View.VISIBLE
        btnSend.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = QueryRequest(query = query)
                val response = RetrofitClient.apiService.queryRAG(request)

                withContext(Dispatchers.Main) {
                    progressBarChat.visibility = View.GONE
                    btnSend.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val botMsg = ChatMessage(
                            text = body.answer,
                            isUser = false,
                            sources = body.sources
                        )
                        adapter.addMessage(botMsg)
                        recyclerViewChat.smoothScrollToPosition(adapter.itemCount - 1)
                    } else {
                        val errorMsg = ChatMessage(
                            text = getString(R.string.query_error) + " (${response.code()})",
                            isUser = false
                        )
                        adapter.addMessage(errorMsg)
                        recyclerViewChat.smoothScrollToPosition(adapter.itemCount - 1)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBarChat.visibility = View.GONE
                    btnSend.isEnabled = true
                    val errorMsg = ChatMessage(
                        text = "Error connecting to server: ${e.localizedMessage}",
                        isUser = false
                    )
                    adapter.addMessage(errorMsg)
                    recyclerViewChat.smoothScrollToPosition(adapter.itemCount - 1)
                    Toast.makeText(this@ChatActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
