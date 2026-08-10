package com.blackbox.module.anyclaw.proot

enum class SetupStep {
    IDLE,
    EXTRACTING_ROOTFS,
    CONFIGURING,
    INSTALLING_OPENCLAW,
    SYNCING_AUTH,
    FINISHING
}
