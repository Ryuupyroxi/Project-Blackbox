package com.blackbox.receiver

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.module.adt.runtime.AdtBootReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdtBootReceiverTest {

    @Test
    fun receiver_isRegisteredInManifest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        val resolved = context.packageManager.queryBroadcastReceivers(intent, 0)
        assertTrue(resolved.any { it.activityInfo.name == "com.blackbox.module.adt.runtime.AdtBootReceiver" })
    }

    @Test
    fun receiver_doesNotCrashOnBootIntent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val receiver = AdtBootReceiver()
        // Should not throw even if services are not fully initialized
        receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))
        assertTrue(true)
    }

    @Test
    fun receiver_ignoresNonBootAction() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val receiver = AdtBootReceiver()
        receiver.onReceive(context, Intent(Intent.ACTION_VIEW))
        assertTrue(true)
    }
}
