package com.blackbox.module.adt.bridge

import com.blackbox.core.module.adt.model.AdtForegroundType
import com.blackbox.core.module.adt.model.AdtReceiverDefinition
import com.blackbox.core.module.adt.model.AdtServiceDefinition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdtManifestMapperTest {

    @Test
    fun services_returnsNonEmptyList() {
        val services = AdtManifestMapper.services()
        assertTrue(services.isNotEmpty())
    }

    @Test
    fun services_containsExpectedIds() {
        val ids = AdtManifestMapper.services().map { it.id }
        assertTrue(ids.contains("llama_service"))
        assertTrue(ids.contains("whisper_service"))
        assertTrue(ids.contains("stable_diffusion_service"))
        assertTrue(ids.contains("model_download_service"))
        assertTrue(ids.contains("agent_foreground_service"))
        assertTrue(ids.contains("adventure_foreground_service"))
        assertTrue(ids.contains("ai_tool_server_service"))
        assertTrue(ids.contains("zim_share_service"))
        assertTrue(ids.contains("litert_worker"))
        assertTrue(ids.contains("video_upscaler_service"))
    }

    @Test
    fun services_nonNullFields() {
        AdtManifestMapper.services().forEach { svc ->
            assertFalse(svc.id.isBlank())
            assertFalse(svc.className.isBlank())
            assertFalse(svc.description.isBlank())
        }
    }

    @Test
    fun bootReceivers_returnsNonEmptyList() {
        val receivers = AdtManifestMapper.bootReceivers()
        assertTrue(receivers.isNotEmpty())
    }

    @Test
    fun bootReceivers_containsExpectedActions() {
        val actions = AdtManifestMapper.bootReceivers().flatMap { it.actions }
        assertTrue(actions.contains("android.intent.action.BOOT_COMPLETED"))
    }
}
