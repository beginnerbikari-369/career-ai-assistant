package com.careerai.domain.model

data class Conversation(
    val id: String,
    val userId: String,
    val title: String,
    val context: ConversationContext,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessageAt: Long,
    val messageCount: Int = 0,
    val isArchived: Boolean = false,
    val messages: List<Message> = emptyList()
)

enum class ConversationContext {
    GENERAL,
    CAREER,
    GOALS,
    HABITS,
    PLANNING
}

data class Message(
    val id: String,
    val conversationId: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val messageType: MessageType = MessageType.TEXT,
    val metadata: Map<String, Any>? = null,
    val tokens: Int? = null,
    val model: String? = null
)

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE,
    FILE
}