package com.blackbox.ai.toolkit

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceToolkit(private val context: Context) {
    
    data class ToolResult(
        val tool: String,
        val success: Boolean,
        val output: String,
        val error: String? = null
    )
    
    private val _tools = MutableStateFlow(toolCategories)
    val tools: StateFlow<List<ToolCategory>> = _tools.asStateFlow()
    
    data class ToolCategory(
        val name: String,
        val icon: String,
        val tools: List<Tool>,
        val enabled: Boolean = true
    )
    
    data class Tool(
        val id: String,
        val name: String,
        val icon: String,
        val description: String,
        val command: String,
        val available: Boolean = true
    )
    
    companion object {
        val toolCategories = listOf(
            ToolCategory(
                name = "Termux API",
                icon = "📱",
                tools = listOf(
                    Tool("clipboard-get", "Clipboard Get", "📋", "Read clipboard", "termux-clipboard-get"),
                    Tool("clipboard-set", "Clipboard Set", "📋", "Set clipboard", "termux-clipboard-set"),
                    Tool("notification", "Notification", "🔔", "Send notification", "termux-notification"),
                    Tool("toast", "Toast", "💬", "Show toast", "termux-toast"),
                    Tool("battery-status", "Battery", "🔋", "Battery status", "termux-battery-status"),
                    Tool("screenshot", "Screenshot", "📸", "Capture screen", "termux-screenshot"),
                    Tool("camera-photo", "Camera", "📷", "Take photo", "termux-camera-photo"),
                    Tool("torch", "Torch", "🔦", "Toggle flashlight", "termux-torch"),
                    Tool("wifi-connectioninfo", "WiFi Info", "📶", "WiFi info", "termux-wifi-connectioninfo"),
                    Tool("location", "Location", "📍", "Get location", "termux-location"),
                    Tool("calendar-list", "Calendar", "📅", "List events", "termux-calendar-list"),
                    Tool("calendar-insert", "Calendar Insert", "📅", "Add event", "termux-calendar-insert"),
                    Tool("vibrate", "Vibrate", "📳", "Haptic feedback", "termux-vibrate"),
                    Tool("volume", "Volume", "🔉", "Set volume", "termux-volume")
                )
            ),
            ToolCategory(
                name = "Shizuku",
                icon = "⚡",
                tools = listOf(
                    Tool("pm", "Package Manager", "📦", "Manage packages", "shizuku pm"),
                    Tool("am", "App Manager", "🎮", "Control apps", "shizuku am"),
                    Tool("settings", "Settings", "⚙️", "System settings", "shizuku settings"),
                    Tool("dumpsys", "Diagnostics", "📊", "System info", "shizuku dumpsys"),
                    Tool("input", "Input", "👆", "Tap/swipe/type", "shizuku input"),
                    Tool("screencap", "Screen Capture", "📸", "Screenshot", "shizuku screencap"),
                    Tool("ls", "List Files", "📂", "List directory", "shizuku ls"),
                    Tool("cat", "Read File", "📄", "Read file", "shizuku cat")
                )
            ),
            ToolCategory(
                name = "Dev Tools",
                icon = "🛠",
                tools = listOf(
                    Tool("system-health", "System Health", "📈", "CPU/RAM/disk", "system-health"),
                    Tool("log-tools", "Log Tools", "📋", "Analyze logs", "log-tools"),
                    Tool("git-intel", "Git Intel", "🔍", "Git history", "git-intel"),
                    Tool("file-finder", "File Finder", "📂", "Find files", "file-finder"),
                    Tool("dep-scan", "Dependency Scan", "📦", "Scan deps", "dep-scan")
                )
            )
        )
    }
    
    suspend fun executeTermux(toolId: String, args: String = ""): ToolResult {
        return try {
            val command = when (toolId) {
                "clipboard-get" -> "termux-clipboard-get"
                "clipboard-set" -> "termux-clipboard-set \"$args\""
                "notification" -> "termux-notification -t \"Blackbox\" -c \"$args\""
                "toast" -> "termux-toast \"$args\""
                "battery-status" -> "termux-battery-status"
                "screenshot" -> "termux-screenshot"
                "camera-photo" -> "termux-camera-photo /sdcard/Download/photo.jpg"
                "torch" -> "termux-torch $args"
                "wifi-connectioninfo" -> "termux-wifi-connectioninfo"
                "location" -> "termux-location"
                "calendar-list" -> "termux-calendar-list"
                "calendar-insert" -> "termux-calendar-insert -t \"$args\""
                "vibrate" -> "termux-vibrate -d 300"
                "volume" -> "termux-volume media $args"
                else -> return ToolResult(toolId, false, "Unknown tool: $toolId")
            }
            
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            
            ToolResult(
                tool = toolId,
                success = process.waitFor() == 0,
                output = output,
                error = error.ifEmpty { null }
            )
        } catch (e: Exception) {
            ToolResult(toolId, false, "", e.message)
        }
    }
    
    suspend fun executeShizuku(toolId: String, args: String = ""): ToolResult {
        return try {
            val command = when (toolId) {
                "pm" -> "shizuku pm $args"
                "am" -> "shizuku am $args"
                "settings" -> "shizuku settings $args"
                "dumpsys" -> "shizuku dumpsys $args"
                "input" -> "shizuku input $args"
                "screencap" -> "shizuku screencap $args"
                "ls" -> "shizuku ls $args"
                "cat" -> "shizuku cat $args"
                else -> return ToolResult(toolId, false, "Unknown tool: $toolId")
            }
            
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            
            ToolResult(
                tool = toolId,
                success = process.waitFor() == 0,
                output = output,
                error = error.ifEmpty { null }
            )
        } catch (e: Exception) {
            ToolResult(toolId, false, "", e.message)
        }
    }
    
    fun toggleCategory(categoryName: String) {
        val current = _tools.value.toMutableList()
        val index = current.indexOfFirst { it.name == categoryName }
        if (index >= 0) {
            current[index] = current[index].copy(enabled = !current[index].enabled)
            _tools.value = current
        }
    }
    
    fun isToolAvailable(toolId: String): Boolean {
        return _tools.value.flatMap { it.tools }.any { it.id == toolId && it.available }
    }
}
