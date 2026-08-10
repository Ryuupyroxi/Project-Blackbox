package com.blackbox.module.adt.service

import com.blackbox.core.module.adt.model.AdtServiceDefinition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdtServiceCatalogTest {

    @Test
    fun services_hasExactCount() {
        assertEquals(10, AdtServiceCatalog.services.size)
    }

    @Test
    fun services_allHaveUniqueIds() {
        val ids = AdtServiceCatalog.services.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun services_nonEmptyClassNames() {
        AdtServiceCatalog.services.forEach { svc ->
            assertFalse(svc.className.isBlank())
            assertTrue(svc.className.startsWith("com.blackbox.module.adt.runtime."))
        }
    }

    @Test
    fun bootReceivers_hasBootReceiver() {
        val ids = AdtServiceCatalog.bootReceivers.map { it.id }
        assertTrue(ids.contains("boot_receiver"))
    }

    @Test
    fun bootReceivers_hasZimDownloadReceiver() {
        val ids = AdtServiceCatalog.bootReceivers.map { it.id }
        assertTrue(ids.contains("zim_download_receiver"))
    }
}
