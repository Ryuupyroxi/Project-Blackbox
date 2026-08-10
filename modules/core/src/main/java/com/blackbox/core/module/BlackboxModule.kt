package com.blackbox.core.module

import android.content.Context
import java.io.File

interface BlackboxModule {
    fun id(): String
    fun version(): String
    fun description(): String
    fun onLoad(context: Context, classLoader: ClassLoader)
    fun onUnload()
}
