package com.blackbox.ai.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

object ProcessManager {
    
    data class ProcessInfo(
        val id: String,
        val command: String,
        val pid: Int? = null,
        val startTime: Long = System.currentTimeMillis(),
        val status: ProcessStatus = ProcessStatus.STARTING,
        val metadata: Map<String, String> = emptyMap()
    )
    
    enum class ProcessStatus {
        STARTING, RUNNING, STOPPED, CRASHED, RESTARTING
    }
    
    private val _processes = MutableStateFlow<Map<String, ProcessInfo>>(emptyMap())
    val processes: StateFlow<Map<String, ProcessInfo>> = _processes.asStateFlow()
    
    private val processMap = ConcurrentHashMap<String, ProcessInfo>()
    
    fun startProcess(id: String, command: String, metadata: Map<String, String> = emptyMap()): ProcessInfo {
        val info = ProcessInfo(id = id, command = command, status = ProcessStatus.RUNNING, metadata = metadata)
        processMap[id] = info
        _processes.value = processMap.toMap()
        return info
    }
    
    fun stopProcess(id: String) {
        processMap[id]?.let { info ->
            processMap[id] = info.copy(status = ProcessStatus.STOPPED)
            _processes.value = processMap.toMap()
        }
    }
    
    fun health(id: String): ProcessInfo? = processMap[id]
    fun list(): List<ProcessInfo> = processMap.values.toList()
    fun isRunning(id: String): Boolean = processMap[id]?.status == ProcessStatus.RUNNING
    fun getRunningCount(): Int = processMap.values.count { it.status == ProcessStatus.RUNNING }
}
