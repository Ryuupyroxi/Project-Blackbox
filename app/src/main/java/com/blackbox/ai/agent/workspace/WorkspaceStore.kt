package com.blackbox.ai.agent.workspace

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Execution channel for a workspace. Every workspace can run through any of:
 *  - [WorkspaceChannel.LOCAL]: on-device embedded/local runtime
 *  - [WorkspaceChannel.SSH]: the local Termux/Ubuntu SSH channel (Blackbox style)
 *  - [WorkspaceChannel.KAI]: the Kai-style assistant daemon (voice-first)
 */
enum class WorkspaceChannel(val label: String) {
    LOCAL("Local"),
    SSH("SSH"),
    KAI("Kai")
}

/**
 * A user-managed agent workspace. Blackbox supports any number of workspaces
 * beyond the Blackbox default ("default_project"). Each workspace carries its own
 * execution channel so the same workspace can run locally, over SSH, or via
 * the Kai assistant.
 */
data class AgentWorkspace(
    val id: String,
    val name: String,
    val folder: String,
    val channel: WorkspaceChannel,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Persists the list of agent workspaces and grants for assistant feature access.
 * The Blackbox default workspace is always seeded first so existing functionality is
 * never lost when merging.
 */
class WorkspaceStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("blackbox_workspaces", Context.MODE_PRIVATE)

    @Synchronized
    fun list(): List<AgentWorkspace> {
        val raw = prefs.getString(KEY_WORKSPACES, null) ?: return defaultWorkspaces()
        return runCatching {
            val arr = JSONArray(raw)
            val out = mutableListOf<AgentWorkspace>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                out += AgentWorkspace(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    folder = obj.getString("folder"),
                    channel = runCatching { WorkspaceChannel.valueOf(obj.getString("channel")) }
                        .getOrDefault(WorkspaceChannel.LOCAL),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
            if (out.isEmpty()) defaultWorkspaces() else out
        }.getOrDefault(defaultWorkspaces())
    }

    @Synchronized
    fun save(workspaces: List<AgentWorkspace>) {
        val arr = JSONArray()
        for (ws in workspaces) {
            arr.put(
                JSONObject()
                    .put("id", ws.id)
                    .put("name", ws.name)
                    .put("folder", ws.folder)
                    .put("channel", ws.channel.name)
                    .put("createdAt", ws.createdAt)
            )
        }
        prefs.edit().putString(KEY_WORKSPACES, arr.toString()).apply()
    }

    fun add(name: String, folder: String, channel: WorkspaceChannel): AgentWorkspace {
        val normalizedFolder = folder.trim().replace(Regex("[^A-Za-z0-9_.-]"), "_")
            .ifBlank { "project_${System.currentTimeMillis()}" }
        val ws = AgentWorkspace(
            id = "ws_${System.currentTimeMillis()}",
            name = name.trim().ifBlank { normalizedFolder },
            folder = normalizedFolder,
            channel = channel
        )
        val updated = list() + ws
        save(updated)
        return ws
    }

    fun update(workspace: AgentWorkspace) {
        save(list().map { if (it.id == workspace.id) workspace else it })
    }

    fun delete(id: String) {
        save(list().filterNot { it.id == id })
    }

    fun switchTo(workspace: AgentWorkspace) {
        prefs.edit().putString(KEY_ACTIVE_ID, workspace.id).apply()
    }

    fun active(): AgentWorkspace {
        val activeId = prefs.getString(KEY_ACTIVE_ID, null) ?: return list().first()
        return list().firstOrNull { it.id == activeId } ?: list().first()
    }

    private fun defaultWorkspaces(): List<AgentWorkspace> = listOf(
        AgentWorkspace(
            id = "ws_default",
            name = "Default",
            folder = "default_project",
            channel = WorkspaceChannel.LOCAL
        )
    )

    companion object {
        private const val KEY_WORKSPACES = "workspaces"
        private const val KEY_ACTIVE_ID = "active_workspace_id"
    }
}

/**
 * Assistant feature-access authorization. The Kai-style assistant can only use
 * in-app features (Organizer, notes, calendar, Kiwix, PDF, Tama, ...) that the
 * user has explicitly granted. Grants are persisted so they survive restarts.
 */
class FeatureAccessStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("blackbox_feature_access", Context.MODE_PRIVATE)

    fun granted(): Set<String> = prefs.getStringSet(KEY_GRANTED, emptySet()) ?: emptySet()

    fun grant(feature: String) {
        val updated = (granted() + feature).toSet()
        prefs.edit().putStringSet(KEY_GRANTED, updated).apply()
    }

    fun revoke(feature: String) {
        val updated = (granted() - feature).toSet()
        prefs.edit().putStringSet(KEY_GRANTED, updated).apply()
    }

    fun isGranted(feature: String): Boolean = feature in granted()

    companion object {
        const val FEATURE_ORGANIZER = "organizer"
        const val FEATURE_NOTES = "notes"
        const val FEATURE_CALENDAR = "calendar"
        const val FEATURE_KIWIX = "kiwix"
        const val FEATURE_PDF = "pdf"
        const val FEATURE_TAMA = "tama"
        const val FEATURE_MODELS = "models"
        const val FEATURE_CHAT = "chat"

        fun allFeatures(): List<Pair<String, String>> = listOf(
            FEATURE_ORGANIZER to "Organizer",
            FEATURE_NOTES to "Notes",
            FEATURE_CALENDAR to "Calendar",
            FEATURE_KIWIX to "Kiwix",
            FEATURE_PDF to "PDF tools",
            FEATURE_TAMA to "P.E.T.",
            FEATURE_MODELS to "Models",
            FEATURE_CHAT to "Chat"
        )

        private const val KEY_GRANTED = "granted_features"
    }
}
