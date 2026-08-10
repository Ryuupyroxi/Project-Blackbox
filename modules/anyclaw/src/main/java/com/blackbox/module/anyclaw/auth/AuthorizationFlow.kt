package com.blackbox.module.anyclaw.auth

data class AuthorizationFlow(
    val authUrl: String = "",
    val codeVerifier: String = "",
    val state: String = "",
    val completed: Boolean = false,
    val error: String? = null
)
