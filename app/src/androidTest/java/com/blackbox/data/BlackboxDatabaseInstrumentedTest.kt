package com.blackbox.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.core.data.ChannelConversationEntity
import com.blackbox.core.data.ChannelMessageEntity
import com.blackbox.core.data.BlackboxDatabase
import com.blackbox.core.data.ChannelConversationDao
import com.blackbox.core.data.ChannelMessageDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlackboxDatabaseInstrumentedTest {

    private lateinit var db: BlackboxDatabase
    private lateinit var channelConvDao: ChannelConversationDao
    private lateinit var channelMsgDao: ChannelMessageDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BlackboxDatabase::class.java, "test-db")
            .allowMainThreadQueries()
            .build()
        channelConvDao = db.channelConversationDao()
        channelMsgDao = db.channelMessageDao()
    }

    @Test
    fun conversation_insertAndRead() = runBlocking {
        val conv = ChannelConversationEntity(
            id = "c1",
            channelType = "discord",
            channelId = "123",
            title = "Test",
            createdAt = 100L,
            updatedAt = 100L
        )
        channelConvDao.insert(conv)
        val all = channelConvDao.getAll()
        assertEquals(1, all.size)
        assertEquals("Test", all[0].title)
    }

    @Test
    fun message_insertAndRead() = runBlocking {
        val conv = ChannelConversationEntity(id = "c2", channelType = "discord", channelId = "456", title = "Conv2", createdAt = 200L, updatedAt = 200L)
        channelConvDao.insert(conv)
        val msg = ChannelMessageEntity(
            id = "m1",
            conversationId = "c2",
            sender = "user",
            content = "hello",
            timestamp = 100L
        )
        channelMsgDao.insert(msg)
        val msgs = channelMsgDao.getMessages("c2")
        assertEquals(1, msgs.size)
        assertEquals("hello", msgs[0].content)
    }

    @Test
    fun conversation_deleteCascades() = runBlocking {
        val conv = ChannelConversationEntity(id = "c3", channelType = "discord", channelId = "789", title = "Del", createdAt = 300L, updatedAt = 300L)
        channelConvDao.insert(conv)
        val all = channelConvDao.getAll()
        channelConvDao.delete(all[0].id)
        val after = channelConvDao.getAll()
        assertEquals(0, after.size)
    }
}
