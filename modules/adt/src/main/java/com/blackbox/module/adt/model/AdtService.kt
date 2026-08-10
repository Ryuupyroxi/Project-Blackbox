package com.blackbox.module.adt.model

// NOTE: AdtForegroundType, AdtServiceDefinition and AdtReceiverDefinition now
// live in `:modules:core` (com.blackbox.core.module.contract) as shared
// contracts. Only ADT-specific types remain here.

data class AdtPermissionDefinition(
    val name: String,
    val required: Boolean,
    val rationale: String
)
