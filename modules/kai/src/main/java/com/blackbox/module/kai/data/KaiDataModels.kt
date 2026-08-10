package com.blackbox.module.kai.data

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val provider: String = "openai",
    val modelId: String = "",
    val apiKey: String = "",
    val baseUrl: String = "",
    val notificationsEnabled: Boolean = true,
    val emailEnabled: Boolean = false,
    val smsEnabled: Boolean = false
)

@Serializable
data class AssistantTurn(
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Attachment(
    val mimeType: String = "",
    val uri: String = "",
    val size: Long = 0
) {
    companion object {
        val Empty = Attachment()
    }
}

@Serializable
enum class BailoutReason { CONTEXT_LIMIT, API_ERROR, TIMEOUT, USER_CANCELLED }

@Serializable
data class ChatPromptRuntimeContext(
    val systemPrompt: String = "",
    val temperature: Double = 0.7,
    val maxTokens: Int = 1024
)

@Serializable
enum class ChatPromptUiMode { CHAT, COMPLETION, ASSISTANT }

@Serializable
data class ConversationsData(
    val conversations: List<Conversation> = emptyList()
)

@Serializable
data class CuratedModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val capabilities: List<String> = emptyList()
)

@Serializable
data class DataRepository(
    val provider: String = "",
    val apiKey: String = ""
)

@Serializable
data class EmailAccount(
    val id: String,
    val email: String,
    val imapServer: String,
    val smtpServer: String,
    val username: String,
    val password: String
)

@Serializable
data class EmailMessage(
    val id: String,
    val accountId: String,
    val subject: String,
    val body: String,
    val fromAddr: String,
    val toAddr: String,
    val timestamp: Long
)

@Serializable
data class EmailStore(
    val accounts: List<EmailAccount> = emptyList(),
    val messages: List<EmailMessage> = emptyList()
)

@Serializable
data class EmailSyncState(
    val accountId: String,
    val lastSync: Long,
    val pendingCount: Int
)

@Serializable
enum class FallbackStatus { NONE, FALLBACK_ACTIVE, FALLBACK_FAILED }

@Serializable
data class FileCategory(
    val name: String,
    val extensions: List<String> = emptyList()
)

@Serializable
enum class FreeMode { NONE, LIMITED, FULL }

@Serializable
data class FreeProviderSuggestion(
    val provider: String,
    val modelId: String,
    val reason: String = ""
)

@Serializable
data class FreeTierModels(
    val provider: String,
    val models: List<String> = emptyList()
)

@Serializable
data class HeartbeatConfig(
    val enabled: Boolean = false,
    val intervalMinutes: Int = 60,
    val prompt: String = ""
)

@Serializable
data class HeartbeatLogEntry(
    val timestamp: Long,
    val event: String,
    val detail: String = ""
)

@Serializable
data class HeartbeatManager(
    val config: HeartbeatConfig = HeartbeatConfig(),
    val logs: List<HeartbeatLogEntry> = emptyList()
)

@Serializable
data class HeartbeatPendingEmail(
    val accountId: String,
    val lastCheck: Long
)

@Serializable
data class HeartbeatPendingNotification(
    val key: String,
    val lastCheck: Long
)

@Serializable
data class HeartbeatPendingSms(
    val threadId: String,
    val lastCheck: Long
)

@Serializable
data class HeartbeatPromotionCandidate(
    val memoryId: String,
    val score: Int
)

@Serializable
data class ImportSection(
    val key: String,
    val label: String,
    val selected: Boolean = false
)

@Serializable
data class LoopChatResult(
    val response: String,
    val toolCalls: List<String> = emptyList(),
    val iterations: Int = 0
)

@Serializable
enum class MemoryCategory { FACT, PREFERENCE, TASK, NOTE }

@Serializable
data class MemoryEntry(
    val id: String,
    val category: MemoryCategory,
    val content: String,
    val strength: Int = 1,
    val lastReinforced: Long = System.currentTimeMillis()
)

@Serializable
data class MemoryStore(
    val entries: List<MemoryEntry> = emptyList()
)

@Serializable
data class ModelCapabilities(
    val supportsImages: Boolean = false,
    val supportsTools: Boolean = false,
    val maxContextTokens: Int = 4096
)

@Serializable
data class ModelCatalog(
    val models: List<ModelDefinition> = emptyList()
)

@Serializable
data class NotificationRecord(
    val key: String,
    val title: String,
    val text: String,
    val timestamp: Long
)

@Serializable
data class NotificationStore(
    val records: List<NotificationRecord> = emptyList()
)

@Serializable
data class NotificationSyncState(
    val lastSync: Long,
    val unreadCount: Int
)

@Serializable
data class PendingQueue<T>(
    val items: List<T> = emptyList()
)

@Serializable
data class PendingTaskPartition(
    val id: String,
    val status: String
)

@Serializable
data class ReasoningRequestMode(
    val mode: String = "default",
    val detail: String = ""
)

@Serializable
data class ScheduledTask(
    val id: String,
    val trigger: TaskTrigger,
    val action: String,
    val enabled: Boolean = true,
    val lastRun: Long = 0,
    val nextRun: Long = 0
) {
    companion object {
        val Empty = ScheduledTask(id = "", trigger = TaskTrigger.IMMEDIATE, action = "")
    }
}

@Serializable
data class SettingsConversationPersistence(
    val enabled: Boolean = true,
    val maxHistory: Int = 100
)

@Serializable
data class SmsDraft(
    val id: String,
    val to: String,
    val body: String,
    val timestamp: Long
)

@Serializable
data class SmsDraftStore(
    val drafts: List<SmsDraft> = emptyList()
)

@Serializable
data class SmsMessage(
    val id: String,
    val threadId: String,
    val address: String,
    val body: String,
    val timestamp: Long,
    val isIncoming: Boolean
)

@Serializable
data class SmsStore(
    val messages: List<SmsMessage> = emptyList()
)

@Serializable
data class SmsSyncState(
    val lastSync: Long,
    val unreadCount: Int
)

@Serializable
enum class SystemPromptVariant { DEFAULT, CODING, CREATIVE, ANALYSIS }

@Serializable
data class TaskExecutionLogEntry(
    val taskId: String,
    val timestamp: Long,
    val status: String,
    val output: String = ""
)

@Serializable
data class TaskStore(
    val tasks: List<ScheduledTask> = emptyList()
)

@Serializable
data class TaskTrigger(
    val type: String,
    val cron: String? = null,
    val delayMinutes: Int? = null
) {
    companion object {
        val IMMEDIATE = TaskTrigger(type = "immediate")
        val HEARTBEAT = TaskTrigger(type = "heartbeat")
    }
}

@Serializable
enum class TaskStatus { PENDING, RUNNING, COMPLETED, FAILED }

@Serializable
data class ToolExecutor(
    val name: String,
    val parameters: Map<String, String> = emptyMap()
)

@Serializable
data class ToolLoopStrategy(
    val maxIterations: Int = 5,
    val parallel: Boolean = false
) {
    interface DefaultImpls
}

@Serializable
data class UiSubmission(
    val text: String = "",
    val attachmentUri: String? = null
)
