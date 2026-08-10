package com.blackbox.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper

/**
 * Minimal Context stub for JVM unit tests.
 * Only implements what the current code paths actually call.
 */
class TestContext : Context {
    override fun getApplicationContext(): Context = this
    override fun getPackageName(): String = "com.blackbox.test"
    override fun getClassLoader(): ClassLoader = javaClass.classLoader!!
    override fun getSystemService(name: String): Any = throw UnsupportedOperationException("TestContext: $name")
    override fun <T> getSystemService(serviceClass: Class<T>): T = throw UnsupportedOperationException("TestContext: $serviceClass")
    override fun getMainLooper(): android.os.Looper = Looper.getMainLooper()
    override fun getApplicationInfo(): android.content.pm.ApplicationInfo = throw UnsupportedOperationException()
    override fun getPackageManager(): android.content.pm.PackageManager = throw UnsupportedOperationException()
    override fun getContentResolver(): android.content.ContentResolver = throw UnsupportedOperationException()
    override fun getMainExecutor(): java.util.concurrent.Executor = Handler(Looper.getMainLooper()).asExecutor
    override fun getAssets(): android.content.res.AssetManager = throw UnsupportedOperationException()
    override fun getResources(): android.content.res.Resources = throw UnsupportedOperationException()
    override fun createCredentialProtectedStorageContext(): Context = this
    override fun createDeviceProtectedStorageContext(): Context = this
    override fun startActivity(intent: Intent) {}
    override fun startActivity(intent: Intent, options: Bundle) {}
    override fun startActivities(intents: Array<out Intent>) {}
    override fun startActivities(intents: Array<out Intent>, options: Bundle) {}
    override fun startService(service: Intent): ComponentName? = null
    override fun stopService(service: Intent): Boolean = false
    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean = false
    override fun unbindService(conn: ServiceConnection) {}
    override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: IntentFilter): Intent? = null
    override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: IntentFilter, flags: Int): Intent? = null
    override fun unregisterReceiver(receiver: android.content.BroadcastReceiver) {}
    override fun sendBroadcast(intent: Intent) {}
    override fun sendBroadcastAsUser(intent: Intent, user: android.os.UserHandle) {}
    override fun sendOrderedBroadcast(intent: Intent, receiverPermission: String?) {}
    override fun sendOrderedBroadcastAsUser(intent: Intent, user: android.os.UserHandle, receiverPermission: String?, resultReceiver: android.content.BroadcastReceiver?, scheduler: Any?, initialCode: Int, initialData: Bundle, initialExtras: Bundle) {}
    override fun sendStickyBroadcast(intent: Intent) {}
    override fun sendStickyBroadcastAsUser(intent: Intent, user: android.os.UserHandle) {}
    override fun sendStickyOrderedBroadcast(intent: Intent, stickyReceiver: android.content.BroadcastReceiver?, resultHandler: Handler?, initialCode: Int, initialData: Bundle, initialExtras: Bundle) {}
    override fun sendStickyOrderedBroadcastAsUser(intent: Intent, user: android.os.UserHandle, stickyReceiver: android.content.BroadcastReceiver?, resultHandler: Handler?, initialCode: Int, initialData: Bundle, initialExtras: Bundle) {}
    override fun removeStickyBroadcast(intent: Intent) {}
    override fun getSharedPreferences(name: String, mode: Int): android.content.SharedPreferences = throw UnsupportedOperationException()
    override fun openFileInput(name: String): java.io.FileInputStream = throw UnsupportedOperationException()
    override fun openFileOutput(name: String, mode: Int): java.io.FileOutputStream = throw UnsupportedOperationException()
    override fun deleteFile(name: String): Boolean = false
    override fun getFileStreamPath(name: String): java.io.File = throw UnsupportedOperationException()
    override fun getString(resId: Int): String = throw UnsupportedOperationException()
    override fun getString(resId: Int, vararg formatArgs: Any): String = throw UnsupportedOperationException()
    override fun getFilesDir(): java.io.File = throw UnsupportedOperationException()
    override fun getCacheDir(): java.io.File = throw UnsupportedOperationException()
    override fun getDir(name: String, mode: Int): java.io.File = throw UnsupportedOperationException()
    override fun getExternalFilesDir(type: String?): java.io.File? = null
    override fun getExternalCacheDir(): java.io.File? = null
    override fun getObbDir(): java.io.File = throw UnsupportedOperationException()
    override fun getObbDirs(): Array<java.io.File> = arrayOf()
    override fun getExternalMediaDirs(): Array<java.io.File> = arrayOf()
    override fun getNoBackupFilesDir(): java.io.File = throw UnsupportedOperationException()
    override fun getCodeCacheDir(): java.io.File = throw UnsupportedOperationException()
    override fun getDeviceProtectedStorageContext(): Context = this
    override fun getCredentialProtectedStorageContext(): Context = this
    override fun createPackageContext(packageName: String, flags: Int): Context = throw UnsupportedOperationException()
    override fun createConfigurationContext(overrideConfiguration: android.content.res.Configuration): Context = this
    override fun createDisplayContext(display: android.view.Display): Context = this
    override fun checkCallingOrSelfPermission(permission: String): Int = throw UnsupportedOperationException()
    override fun checkPermission(permission: String, pid: Int, uid: Int): Int = throw UnsupportedOperationException()
    override fun checkSelfPermission(permission: String): Int = throw UnsupportedOperationException()
    override fun checkUriPermission(uri: android.net.Uri, pid: Int, uid: Int, modeFlags: Int): Int = throw UnsupportedOperationException()
    override fun checkUriPermission(uri: android.net.Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int): Int = throw UnsupportedOperationException()
    override fun enforcePermission(permission: String, pid: Int, uid: Int, message: String?) {}
    override fun enforceCallingOrSelfPermission(permission: String, message: String?) {}
    override fun enforceCallingPermission(permission: String, message: String?) {}
    override fun enforceUriPermission(uri: android.net.Uri, pid: Int, uid: Int, modeFlags: Int, message: String?) {}
    override fun enforceUriPermission(uri: android.net.Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int, message: String?) {}
    override fun grantUriPermission(toPackage: String, uri: android.net.Uri, modeFlags: Int) {}
    override fun revokeUriPermission(uri: android.net.Uri, modeFlags: Int) {}
    override fun revokeUriPermission(toPackage: String, uri: android.net.Uri, modeFlags: Int) {}
    override fun getWallpaper(): android.graphics.drawable.Drawable = throw UnsupportedOperationException()
    override fun setWallpaper(bitmap: android.graphics.Bitmap) {}
    override fun setWallpaper(data: java.io.InputStream) {}
    override fun clearWallpaper() {}
    override fun startInstrumentation(className: ComponentName, profileFile: String?, arguments: Bundle?): ComponentName? = null
    override fun getBasePackageName(): String = packageName
    override fun getOpPackageName(): String = packageName
}
