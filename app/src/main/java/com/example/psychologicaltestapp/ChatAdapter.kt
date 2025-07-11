package com.example.psychologicaltestapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
        val container: View = view.findViewById(R.id.chatMessageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.messageText

        // Alinear a la derecha si es del usuario actual
        val layoutParams = holder.container.layoutParams as ViewGroup.MarginLayoutParams
        if (message.fromUserId == currentUserId) {
            layoutParams.marginStart = 100
            layoutParams.marginEnd = 0
        } else {
            layoutParams.marginStart = 0
            layoutParams.marginEnd = 100
        }
        holder.container.layoutParams = layoutParams
    }

    override fun getItemCount(): Int = messages.size
}
