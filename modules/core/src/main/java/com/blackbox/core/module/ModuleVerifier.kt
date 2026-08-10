package com.blackbox.core.module

import java.security.MessageDigest

object ModuleVerifier {
    fun verify(manifest: ModuleManifest, actualBytes: ByteArray): Boolean {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(actualBytes)
            .joinToString("") { "%02x".format(it) }
        return digest == manifest.sha256
    }
}
