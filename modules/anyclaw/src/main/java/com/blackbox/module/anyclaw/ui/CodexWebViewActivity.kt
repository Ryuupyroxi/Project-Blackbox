package com.blackbox.module.anyclaw.ui

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import com.blackbox.module.anyclaw.data.PreferencesManager
import com.blackbox.core.module.ModuleBus

class CodexWebViewActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)
        ModuleBus.publish(ModuleEvent("anyclaw", "codex_webview_created"))
    }
}
