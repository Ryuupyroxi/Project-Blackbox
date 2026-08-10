package com.blackbox.module.anyclaw.proot

data class BundleUpdateAttemptResult(
    val outcome: BundleUpdateOutcome,
    val failureType: BundleUpdateFailureType? = null,
    val message: String = ""
)
