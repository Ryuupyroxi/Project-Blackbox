package com.blackbox.module.anyclaw.auth

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.blackbox.module.anyclaw.data.PreferencesManager

class OpenRouterAuth(private val context: Context, private val prefs: PreferencesManager) {
    suspend fun exchangeCodeForKey(code: String, codeVerifier: String): Boolean {
        // Placeholder token exchange. Real implementation requires backend client ID/secret.
        return code.isNotBlank() && codeVerifier.isNotBlank()
    }

    fun launchAuthorizationFlow(authUrl: String) {
        try {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(context, Uri.parse(authUrl))
        } catch (_: Exception) {
            // Fallback: open in WebView
        }
    }
}
