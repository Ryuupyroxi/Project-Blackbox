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
    private val binder = Binder()

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

    inner class Binder : Binder() {
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
        serviceScope.cancel()
        stopSharing()
    }

    private fun startSharing(zimFile: File): Boolean {
        if (_sharing.value) return false
        if (!zimFile.exists()) return false
        shareDir = zimFile.parentFile?.let { File(it, zimFile.nameWithoutExtension) }
        shareDir?.mkdirs()
        val ifaces = getNonLoopbackInterfaces()
        if (ifaces.isEmpty()) return false
        _addresses.value = ifaces
        return try {
            serverSocket = java.net.ServerSocket(0)
            _port.value = serverSocket!!.localPort
            _sharing.value = true
            serviceScope.launch { acceptLoop() }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun stopSharing() {
        _sharing.value = false
        runCatching { serverSocket?.close() }
        serverSocket = null
        _port.value = 0
    }

    private suspend fun acceptLoop() {
        val sock = serverSocket ?: return
        while (_sharing.value) {
            try {
                val client = sock.accept() ?: break
                serviceScope.launch {
                    _connections.value = _connections.value + 1
                    client.use { c ->
                        c.getInputStream().bufferedReader().use { reader ->
                            val request = reader.readLine() ?: return@use
                            if (request.startsWith("GET ")) {
                                serveFile(client, request)
                            }
                        }
                    }
                    _connections.value = (_connections.value - 1).coerceAtLeast(0)
                }
            } catch (e: Exception) {
                if (_sharing.value) kotlinx.coroutines.delay(100)
            }
        }
    }

    private fun serveFile(client: java.net.Socket, request: String) {
        val dir = shareDir ?: return
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
