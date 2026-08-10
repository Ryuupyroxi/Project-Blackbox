package com.blackbox.module.kai.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "kai_conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "kai_messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val timestamp: Long
)

@Dao
interface ConversationQueries {
    @Query("SELECT * FROM kai_conversations ORDER BY updatedAt DESC")
    suspend fun selectAllConversations(): List<ConversationEntity>

    @Query("SELECT * FROM kai_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun selectAllMessages(conversationId: String): List<MessageEntity>
}

@Dao
interface MessageQueries {
    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("DELETE FROM kai_messages WHERE conversationId = :conversationId")
    suspend fun deleteForConversation(conversationId: String)
}
