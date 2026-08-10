package com.blackbox.receiver

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.module.adt.runtime.ZimDownloadReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZimDownloadReceiverTest {

    @Test
    fun receiver_isRegisteredInManifest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("content://test/foo.zim"))
        val resolved = context.packageManager.queryBroadcastReceivers(intent, 0)
        assertTrue(resolved.any { it.activityInfo.name == "com.blackbox.module.adt.runtime.ZimDownloadReceiver" })
    }

    @Test
    fun receiver_doesNotCrashOnViewIntent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val receiver = ZimDownloadReceiver()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("file:///sdcard/foo.zim"))
        receiver.onReceive(context, intent)
        assertTrue(true)
    }
}
