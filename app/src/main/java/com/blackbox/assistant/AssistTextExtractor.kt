package com.blackbox.assistant

import android.app.assist.AssistStructure
import android.content.Context
import android.view.View

object AssistTextExtractor {
    fun extract(structure: AssistStructure?): String? {
        if (structure == null) return null
        val text = StringBuilder()
        try {
            structure.visit(object : AssistStructure.ViewNodeVisitor {
                override fun onTextView(textView: View): Boolean {
                    val t = textView.text?.toString()
                    if (!t.isNullOrBlank()) text.append(t).append(' ')
                    return true
                }

                override fun onWebView(webView: View): Boolean = true

                override fun onVisitEnd() {}
            })
        } catch (_: Throwable) {
            // Fallback: ignore malformed assist structure
        }
        val result = text.toString().trim()
        return result.ifBlank { null }
    }
}
