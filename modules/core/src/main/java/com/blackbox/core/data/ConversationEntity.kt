package com.blackbox.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val provider: String,
    val modelId: String,
    val createdAt: Long,
    val updatedAt: Long
)
