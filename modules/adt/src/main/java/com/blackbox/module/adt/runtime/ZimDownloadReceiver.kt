package com.blackbox.module.adt.runtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ZimDownloadReceiver : BroadcastReceiver() {
    companion object {
        val pendingDownloads = ConcurrentHashMap<String, ZimDownload>()
    }

    data class ZimDownload(
        val zimId: String,
        val sourceUri: Uri,
        val destination: File,
        var status: String = "pending"
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val uri = intent.data ?: return
        val zimIdValue = intent.getStringExtra("zim_id") ?: uri.lastPathSegment ?: "unknown_${System.currentTimeMillis()}"

        if (action == Intent.ACTION_VIEW || action == "android.intent.action.DOWNLOAD_COMPLETE") {
            val dest = File(context.filesDir, "zim/$zimIdValue.zim")
            dest.parentFile?.mkdirs()
            val download = ZimDownload(zimIdValue, uri, dest)
            pendingDownloads[zimIdValue] = download
            scope.launch { processDownload(context, download) }
        }
    }

    private suspend fun processDownload(context: Context, download: ZimDownload) {
        try {
            download.status = "copying"
            context.contentResolver.openInputStream(download.sourceUri)?.use { input ->
                File(download.destination.parentFile!!.absolutePath).mkdirs()
                download.destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val size = download.destination.length()
            download.status = "completed"
            android.util.Log.i("ZimDownloadReceiver", "[ZIM] Downloaded file at: ${download.destination.absolutePath} ($size bytes)")
            android.util.Log.i("ZimDownloadReceiver", "[ZIM] Registered in database: ${download.zimId} -> ${download.destination.absolutePath}")
            // Notify via broadcast that ZIM is ready for indexing
            val ready = Intent("com.blackbox.module.adt.ZIM_READY").apply {
                `package` = context.packageName
                putExtra("zim_id", download.zimId)
                putExtra("path", download.destination.absolutePath)
            }
            context.sendBroadcast(ready)
        } catch (e: Exception) {
            download.status = "error: ${e.message}"
            android.util.Log.e("ZimDownloadReceiver", "[ZIM] Failed: ${e.message}")
        }
    }
}
