package com.blackbox.module.anyclaw.proot

import android.content.Context
import com.blackbox.module.anyclaw.data.PreferencesManager
import com.blackbox.module.anyclaw.proot.SetupException
import kotlinx.coroutines.flow.Flow

class SetupManager(private val context: Context, private val prefs: PreferencesManager) {
    data class RootfsSource(val url: String? = null, val asset: String? = null)
    data class OpenClawUpdateInfo(val version: String, val url: String)
    data class OpenClawIncrementalSyncSummary(val synced: Int, val total: Int)

    suspend fun runFullSetup(): Flow<SetupStep> = kotlinx.coroutines.flow.flowOf(SetupStep.COMPLETE)
    suspend fun runFullSetupFromManualRootfs(source: RootfsSource) = false
    suspend fun runRecoveryInstall() = false
    suspend fun runOpenClawManualSync() = 0
    suspend fun installOpenClaw() = false
    suspend fun reinstallOpenClawFromAssets() = false
    suspend fun tryInstallOpenClawIncremental(info: OpenClawUpdateInfo) = false
    suspend fun updateBundleIfNeeded() = false
    suspend fun updateBundleIfNeededWithPolicy() = false
    suspend fun runBundleUpdateWithPolicy() = false
    suspend fun verify() = false
    suspend fun syncCodexAuthToClaude() = false
    suspend fun getOpenClawUpdateInfo(): OpenClawUpdateInfo? = null
    suspend fun getBundleUpdateFailureState(): BundleUpdateFailureState? = null
    suspend fun downloadRootfsFromGitHub(): RootfsSource? = null
    suspend fun extractRootfsFromAssets(): Boolean = false
}
