package com.blackbox.core.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ChannelConversationEntity::class,
        ChannelMessageEntity::class,
        AssistantSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BlackboxDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun channelConversationDao(): ChannelConversationDao
    abstract fun channelMessageDao(): ChannelMessageDao
    abstract fun assistantSessionDao(): AssistantSessionDao
}
