package com.blackbox.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blackbox.module.adt.runtime.WhisperService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WhisperServiceTest {

    @Test
    fun service_startsAndBinds() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, WhisperService::class.java)
        context.startService(intent)
        val connection = object : android.content.ServiceConnection {
            var binder: IBinder? = null
            override fun onServiceConnected(name: ComponentName, service: IBinder) { binder = service }
            override fun onServiceDisconnected(name: ComponentName) { binder = null }
        }
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        assertTrue(connection.binder != null)
        context.unbindService(connection)
        context.stopService(intent)
    }
}
