package com.blackbox.module.anyclaw.proot

sealed interface OpenClawUpdateInfo {
    data object UpToDate : OpenClawUpdateInfo
    data class UpdateAvailable(val bundledVersion: String, val installedVersion: String) : OpenClawUpdateInfo
    data class Error(val message: String) : OpenClawUpdateInfo
}
