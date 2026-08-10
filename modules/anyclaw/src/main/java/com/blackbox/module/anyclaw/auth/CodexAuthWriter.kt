package com.blackbox.module.anyclaw.auth

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

class CodexAuthWriter(private val context: Context) {
    fun writeAuth(token: String) {
        // Auth tokens are stored via PreferencesManager -> SecretStore.
        // This writer keeps the Codex-specific side-effect surface.
    }

    fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    fun codeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
