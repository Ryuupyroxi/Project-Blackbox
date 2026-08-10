package com.blackbox.ui.screen.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ChatMessage(val role: String, val text: String)

@Composable
fun ChatScreen(onBack: () -> Unit = {}) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val input = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Unified Chat", style = MaterialTheme.typography.headlineMedium)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { msg ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "${msg.role}: ${msg.text}", modifier = Modifier.padding(12.dp))
                }
            }
        }
        OutlinedTextField(
            value = input.value,
            onValueChange = { input.value = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
