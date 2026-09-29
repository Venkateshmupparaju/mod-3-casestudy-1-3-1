package project.rag_basedassistant3_1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import project.rag_basedassistant3_1.R
import project.rag_basedassistant3_1.models.ChatMessage

class MessageAdapter(
    private val messages: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
        val tvBotMessage: TextView = itemView.findViewById(R.id.tvBotMessage)
        val tvSources: TextView = itemView.findViewById(R.id.tvSources)
        val containerUser: View = itemView.findViewById(R.id.containerUser)
        val containerBot: View = itemView.findViewById(R.id.containerBot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        if (message.isUser) {
            holder.containerUser.visibility = View.VISIBLE
            holder.containerBot.visibility = View.GONE
            holder.tvUserMessage.text = message.text
        } else {
            holder.containerUser.visibility = View.GONE
            holder.containerBot.visibility = View.VISIBLE
            holder.tvBotMessage.text = message.text

            if (!message.sources.isNullOrEmpty()) {
                holder.tvSources.visibility = View.VISIBLE
                val sourcesText = "Sources: " + message.sources.joinToString(", ")
                holder.tvSources.text = sourcesText
            } else {
                holder.tvSources.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
