package com.blackbox.core.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey

@Entity(tableName = "channel_conversations")
data class ChannelConversationEntity(
    @PrimaryKey val id: String,
    val channelType: String,
    val channelId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "channel_messages")
data class ChannelMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val sender: String,
    val content: String,
    val timestamp: Long
)

@Entity(tableName = "assistant_sessions")
data class AssistantSessionEntity(
    @PrimaryKey val sessionId: String,
    val sourcePackage: String,
    val extractedText: String,
    val routedProvider: String,
    val response: String,
    val timestamp: Long
)

@Dao
interface ChannelConversationDao {
    @Query("SELECT * FROM channel_conversations ORDER BY updatedAt DESC")
    suspend fun getAll(): List<ChannelConversationEntity>

    @Insert
    suspend fun insert(conversation: ChannelConversationEntity)

    @Query("DELETE FROM channel_conversations WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface ChannelMessageDao {
    @Query("SELECT * FROM channel_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessages(conversationId: String): List<ChannelMessageEntity>

    @Insert
    suspend fun insert(message: ChannelMessageEntity)

    @Query("DELETE FROM channel_messages WHERE conversationId = :conversationId")
    suspend fun deleteForConversation(conversationId: String)
}

@Dao
interface AssistantSessionDao {
    @Query("SELECT * FROM assistant_sessions ORDER BY timestamp DESC")
    suspend fun getAll(): List<AssistantSessionEntity>

    @Insert
    suspend fun insert(session: AssistantSessionEntity)

    @Query("DELETE FROM assistant_sessions WHERE sessionId = :sessionId")
    suspend fun delete(sessionId: String)
}
