package com.blackbox.ai.agent.workspace

import android.content.Context
import com.blackbox.ai.service.AgentService

/**
 * Dispatch layer that enforces FeatureAccessStore grants before the assistant
 * can act on in-app features (Organizer, Notes, Calendar, Kiwix, PDF, Tama, etc.).
 *
 * Usage:
 *   val dispatch = FeatureDispatch(context, featureAccessStore)
 *   dispatch.execute(FeatureAccessStore.FEATURE_ORGANIZER) {
 *       // Only runs if user granted Organizer access
 *       agentService.createOrganizerEntry(...)
 *   }
 *
 * If the feature isn't granted, returns Result.failure with a user-facing message.
 */
class FeatureDispatch(
    private val context: Context,
    private val featureAccess: FeatureAccessStore,
) {

    /**
     * Executes the block if the feature is granted; otherwise returns failure.
     */
    fun <T> execute(feature: String, block: () -> T): Result<T> {
        if (!featureAccess.isGranted(feature)) {
            val label = FeatureAccessStore.allFeatures().firstOrNull { it.first == feature }?.second ?: feature
            return Result.failure(SecurityException("Assistant not authorized to use $label. Grant access in Agent Hub."))
        }
        return runCatching { block() }
    }

    /**
     * Suspend version for async operations.
     */
    suspend fun <T> executeSuspend(feature: String, block: suspend () -> T): Result<T> {
        if (!featureAccess.isGranted(feature)) {
            val label = FeatureAccessStore.allFeatures().firstOrNull { it.first == feature }?.second ?: feature
            return Result.failure(SecurityException("Assistant not authorized to use $label. Grant access in Agent Hub."))
        }
        return runCatching { block() }
    }

    /**
     * Checks if a feature is granted without executing anything.
     */
    fun isGranted(feature: String): Boolean = featureAccess.isGranted(feature)

    /**
     * Returns all features with their grant status — useful for UI.
     */
    fun listAll(): List<FeatureStatus> = FeatureAccessStore.allFeatures().map { (key, label) ->
        FeatureStatus(key, label, featureAccess.isGranted(key))
    }

    data class FeatureStatus(
        val key: String,
        val label: String,
        val granted: Boolean
    )

    /**
     * Maps AgentService tool names to feature keys for automatic enforcement.
     * This can be used by the tool executor to gate tool calls.
     */
    companion object {
        // Tool name → required feature
        private val toolToFeature = mapOf(
            "create_organizer_entry" to FeatureAccessStore.FEATURE_ORGANIZER,
            "update_organizer_entry" to FeatureAccessStore.FEATURE_ORGANIZER,
            "delete_organizer_entry" to FeatureAccessStore.FEATURE_ORGANIZER,
            "list_organizer_entries" to FeatureAccessStore.FEATURE_ORGANIZER,
            "search_organizer" to FeatureAccessStore.FEATURE_ORGANIZER,
            "create_note" to FeatureAccessStore.FEATURE_NOTES,
            "read_note" to FeatureAccessStore.FEATURE_NOTES,
            "update_note" to FeatureAccessStore.FEATURE_NOTES,
            "delete_note" to FeatureAccessStore.FEATURE_NOTES,
            "list_notes" to FeatureAccessStore.FEATURE_NOTES,
            "create_calendar_event" to FeatureAccessStore.FEATURE_CALENDAR,
            "read_calendar_event" to FeatureAccessStore.FEATURE_CALENDAR,
            "update_calendar_event" to FeatureAccessStore.FEATURE_CALENDAR,
            "delete_calendar_event" to FeatureAccessStore.FEATURE_CALENDAR,
            "list_calendar_events" to FeatureAccessStore.FEATURE_CALENDAR,
            "kiwix_search" to FeatureAccessStore.FEATURE_KIWIX,
            "kiwix_fetch" to FeatureAccessStore.FEATURE_KIWIX,
            "pdf_extract_text" to FeatureAccessStore.FEATURE_PDF,
            "pdf_search" to FeatureAccessStore.FEATURE_PDF,
            "pdf_summarize" to FeatureAccessStore.FEATURE_PDF,
            "tama_interact" to FeatureAccessStore.FEATURE_TAMA,
            "tama_feed" to FeatureAccessStore.FEATURE_TAMA,
            "tama_play" to FeatureAccessStore.FEATURE_TAMA,
            "model_download" to FeatureAccessStore.FEATURE_MODELS,
            "model_delete" to FeatureAccessStore.FEATURE_MODELS,
            "model_list" to FeatureAccessStore.FEATURE_MODELS,
            "chat_send" to FeatureAccessStore.FEATURE_CHAT,
        )

        fun featureForTool(toolName: String): String? = toolToFeature[toolName]

        fun isToolAllowed(toolName: String, featureAccess: FeatureAccessStore): Boolean {
            val feature = toolToFeature[toolName]
            return feature == null || featureAccess.isGranted(feature)
        }
    }
}
