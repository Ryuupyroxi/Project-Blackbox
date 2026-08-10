package com.blackbox.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDao {
    @Query("SELECT * FROM MessageEntity WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getForConversation(conversationId: String): List<MessageEntity>

    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("DELETE FROM MessageEntity WHERE conversationId = :conversationId")
    suspend fun deleteForConversation(conversationId: String)
}
