package com.blackbox.ai.data

import android.content.Context
// import com.blackbox.ai.data.binary.BinaryRepository
// import com.blackbox.ai.data.model.ModelRepository
// import com.blackbox.ai.data.rag.KnowledgeBaseManager

interface AppContainer {
    // val binaryRepository: BinaryRepository
    // val modelRepository: ModelRepository
    // val knowledgeBaseManager: KnowledgeBaseManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    // Initialize repositories here
}
