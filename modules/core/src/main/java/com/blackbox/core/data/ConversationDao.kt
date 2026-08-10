package com.blackbox.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ConversationDao {
    @Query("SELECT * FROM ConversationEntity ORDER BY updatedAt DESC")
    suspend fun getAll(): List<ConversationEntity>

    @Insert
    suspend fun insert(conversation: ConversationEntity)

    @Query("DELETE FROM ConversationEntity WHERE id = :id")
    suspend fun delete(id: String)
}
