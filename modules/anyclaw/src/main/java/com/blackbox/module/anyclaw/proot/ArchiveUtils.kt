package com.blackbox.module.anyclaw.proot

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class ArchiveUtils(private val context: Context) {
    fun extractTar(source: File, target: File) {
        // Tar extraction stub. Real implementation would use tar library or system tar binary.
    }

    fun extractZip(source: File, target: File) {
        target.mkdirs()
        ZipInputStream(FileInputStream(source)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val outFile = File(target, entry.name)
                if (entry.isDirectory) outFile.mkdirs() else {
                    outFile.parentFile?.mkdirs()
                    outFile.writeBytes(zip.readBytes())
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }
}
