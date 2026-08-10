package com.blackbox.module.adt.runtime

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

class ZimShareService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val binder = LocalBinder()

    private val _sharing = MutableStateFlow(false)
    val sharing: StateFlow<Boolean> = _sharing

    private val _port = MutableStateFlow(0)
    val port: StateFlow<Int> = _port

    private val _addresses = MutableStateFlow<List<String>>(emptyList())
    val addresses: StateFlow<List<String>> = _addresses

    private val _connections = MutableStateFlow(0)
    val connections: StateFlow<Int> = _connections

    @Volatile
    private var serverSocket: java.net.ServerSocket? = null
    @Volatile
    private var shareDir: File? = null

    inner class LocalBinder : Binder() {
        fun startShare(zimPath: String): Boolean = startSharing(File(zimPath))
        fun stopShare() = stopSharing()
        fun isSharing(): Boolean = _sharing.value
        fun getShareUrls(): List<String> {
            val dir = shareDir ?: return emptyList()
            val port = _port.value
            return _addresses.value.map { "http://$it:$port/${dir.name}/" }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        serverSocket?.close()
        stopSharing()
        serviceScope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val path = intent?.getStringExtra("path") ?: return START_NOT_STICKY
        startSharing(File(path))
        return START_STICKY
    }

    private fun startSharing(file: File): Boolean {
        if (_sharing.value) return true
        shareDir = file
        _sharing.value = true
        shareDir?.let { dir ->
            if (dir.exists()) {
                val ads = getNonLoopbackInterfaces()
                _addresses.value = ads
                val port = 8080 + (ads.size * 10)
                _port.value = port
                startHttpServer(dir, port)
                return true
            }
        }
        return false
    }

    private fun stopSharing() {
        if (!_sharing.value) return
        _sharing.value = false
        runCatching { serverSocket?.close() }
        serverSocket = null
        shareDir = null
        _addresses.value = emptyList()
        _port.value = 0
    }

    private fun startHttpServer(dir: File, port: Int) {
        serverSocket = java.net.ServerSocket()
        serverSocket?.bind(java.net.InetSocketAddress(port))
        serviceScope.launch {
            try {
                while (_sharing.value) {
                    val client = serverSocket?.accept() ?: continue
                    serviceScope.launch { handleClient(dir, client) }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun handleClient(dir: File, client: java.net.Socket) {
        try {
            val request = client.getInputStream().bufferedReader().readLine()
                ?: return
            val path = request.substringAfter("GET ").substringBefore(" ").removePrefix("/")
            val file = if (path.isBlank()) File(dir, "index.html") else File(dir, path)
            if (!file.exists() || !file.canonicalPath.startsWith(dir.canonicalPath)) {
                client.getOutputStream().bufferedWriter().use { it.write("HTTP/1.1 404\r\n\r\n") }
                return
            }
            val mime = when (file.extension.lowercase()) {
                "html" -> "text/html"
                "js" -> "application/javascript"
                "css" -> "text/css"
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "zim" -> "application/octet-stream"
                else -> "application/octet-stream"
            }
            client.getOutputStream().buffered().use { out ->
                out.write("HTTP/1.1 200 OK\r\nContent-Type: $mime\r\n\r\n".toByteArray())
                file.inputStream().use { it.copyTo(out) }
            }
        } finally {
            client.close()
        }
    }

    companion object {
        fun getNonLoopbackInterfaces(): List<String> {
            val result = mutableListOf<String>()
            val ifaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (iface in ifaces) {
                if (iface.isLoopback || !iface.isUp) continue
                val addrs = Collections.list(iface.inetAddresses)
                for (addr in addrs) {
                    if (addr is InetAddress && !addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        result.add(addr.hostAddress)
                    }
                }
            }
            return result
        }
    }
}