package com.blackbox.module.anyclaw.platform

import android.content.Context
import com.blackbox.module.anyclaw.bridge.DeviceBridge
import com.blackbox.module.anyclaw.bridge.DiscordBridge
import com.blackbox.module.anyclaw.bridge.TelegramBridge
import com.blackbox.module.anyclaw.bridge.WhatsAppBridge
import com.blackbox.module.anyclaw.data.ChannelConfig
import org.json.JSONArray
import org.json.JSONObject

class ServiceBridge(private val context: Context) {
    private val deviceBridge = DeviceBridge(context)
    private val discordBridge = DiscordBridge(context)
    private val telegramBridge = TelegramBridge(context)
    private val whatsAppBridge = WhatsAppBridge(context)

    fun dispatch(cmd: String, args: JSONArray): JSONObject {
        val cfg = currentChannelConfig()
        return when (cmd) {
            "discord_send" -> {
                val channelId = args.optString(0)
                val content = args.optString(1)
                runCatching {
                    val ok = discordBridge.sendMessage(cfg.discordBotToken.orEmpty(), channelId, content)
                    JSONObject().put("ok", ok).put("bridge", "discord")
                }.getOrElse { JSONObject().put("error", it.message) }
            }
            "telegram_send" -> {
                val chatId = args.optString(0)
                val text = args.optString(1)
                runCatching {
                    val ok = telegramBridge.sendMessage(cfg.telegramBotToken.orEmpty(), chatId, text)
                    JSONObject().put("ok", ok).put("bridge", "telegram")
                }.getOrElse { JSONObject().put("error", it.message) }
            }
            "whatsapp_send" -> {
                val phone = args.optString(0)
                val message = args.optString(1)
                runCatching {
                    val ok = whatsAppBridge.sendMessage(phone, message)
                    JSONObject().put("ok", ok).put("bridge", "whatsapp")
                }.getOrElse { JSONObject().put("error", it.message) }
            }
            else -> deviceBridge.handleCommand(cmd, args)
        }
    }

    fun dispatchByChannel(channel: String, cmd: String, args: JSONArray): JSONObject {
        return when (channel.lowercase()) {
            "discord" -> dispatch("discord_send", args)
            "telegram" -> dispatch("telegram_send", args)
            "whatsapp" -> dispatch("whatsapp_send", args)
            else -> dispatch(cmd, args)
        }
    }

    private fun currentChannelConfig(): ChannelConfig = ChannelConfig()
}
