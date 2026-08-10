package com.blackbox.module.anyclaw.proot

data class OpenClawIncrementalSyncSummary(
    val copied: Int,
    val skipped: Int,
    val bundledOnly: Int
)
