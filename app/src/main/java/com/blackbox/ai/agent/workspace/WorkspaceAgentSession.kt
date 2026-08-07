package com.blackbox.ai.agent.workspace

import android.content.Context
import com.blackbox.ai.engine.AgentEngineAdapter
import com.blackbox.ai.engine.EngineKeysStore
import com.blackbox.ai.engine.SSHConfig
import com.blackbox.ai.service.AgentService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages per-workspace agent sessions. Each workspace keeps its own
 * conversation history, context, and active agent role.
 *
 * When switching workspaces, the AgentService state is preserved in the
 * workspace's folder under /workspace/<folder>/brain/
 */
class WorkspaceAgentSession(
    private val context: Context,
    private val keys: EngineKeysStore,
    private val workspaceStore: WorkspaceStore,
) {

    private val _currentWorkspace = MutableStateFlow<AgentWorkspace>(workspaceStore.active())
    val currentWorkspace: StateFlow<AgentWorkspace> = _currentWorkspace

    private val engineAdapter by lazy { AgentEngineAdapter(context, keys) }

    fun getActiveWorkspace(): AgentWorkspace = _currentWorkspace.value

    fun switchTo(workspace: AgentWorkspace): Result<Unit> = runCatching {
        workspaceStore.switchTo(workspace)
        _currentWorkspace.value = workspace
        // AgentService.setCurrentProjectFolder(workspace.folder) // caller applies
    }

    fun getEngineAdapter(): AgentEngineAdapter = engineAdapter

    /**
     * Returns the channel configured for the active workspace.
     * This determines which execution path the agent uses.
     */
    fun getWorkspaceChannel(): WorkspaceChannel = _currentWorkspace.value.channel

    /**
     * Gets the Termux/SSH config for SSH-channel workspaces.
     */
    fun getTermuxConfig(): SSHConfig {
        val ws = _currentWorkspace.value
        return SSHConfig(
            host = keys.getTermuxHost(),
            port = keys.getTermuxPort(),
            user = keys.getTermuxUser(),
            password = keys.getTermuxPassword()
        )
    }

    /**
     * Creates a FeatureDispatch for the active workspace.
     * In the future, grants could be per-workspace; for now they're global.
     */
    fun createFeatureDispatch(featureAccess: FeatureAccessStore): FeatureDispatch {
        return FeatureDispatch(context, featureAccess)
    }
}
