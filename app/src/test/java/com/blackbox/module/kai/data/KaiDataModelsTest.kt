package com.blackbox.module.kai.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class KaiDataModelsTest {

    @Test
    fun service_defaultsAreStable() {
        val svc = Service()
        assertNotNull(svc.id)
        assertEquals("", svc.name)
        assertEquals("", svc.endpoint)
        assertEquals(ServiceCategory.CHAT, svc.category)
    }

    @Test
    fun service_copyRetainsFields() {
        val original = Service(id = "s1", name = "Kai", endpoint = "http://localhost:8080", category = ServiceCategory.CHAT)
        val copy = original.copy(name = "Kai 2")
        assertEquals("s1", copy.id)
        assertEquals("Kai 2", copy.name)
        assertEquals("http://localhost:8080", copy.endpoint)
        assertEquals(ServiceCategory.CHAT, copy.category)
    }

    @Test
    fun conversation_entityDefaults() {
        val conv = ConversationEntity(id = 1, title = "Test", createdAt = 100L)
        assertEquals("Test", conv.title)
        assertEquals(100L, conv.createdAt)
    }

    @Test
    fun message_entityDefaults() {
        val msg = MessageEntity(id = 1, conversationId = 1, role = "user", text = "hi")
        assertEquals("user", msg.role)
        assertEquals("hi", msg.text)
        assertEquals(1L, msg.conversationId)
    }

    @Test
    fun model_defaultsAreStable() {
        val model = Model()
        assertNotNull(model.id)
        assertEquals("", model.name)
    }

    @Test
    fun modelDefinition_alias_isConsistent() {
        val md = ModelDefinition(id = "m1", name = "M1", provider = "local")
        assertEquals("m1", md.id)
        assertEquals("M1", md.name)
        assertEquals("local", md.provider)
    }
}
