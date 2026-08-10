package com.blackbox.module.anyclaw.data

data class ChannelConfig(
    val discordEnabled: Boolean = false,
    val telegramEnabled: Boolean = false,
    val whatsappEnabled: Boolean = false,
    val discordBotToken: String = "",
    val telegramBotToken: String = "",
    val discordGuildAllowlist: String = "",
    val discordRequireMention: Boolean = false,
    val autoStartSshd: Boolean = false,
    val batteryOptimizationPrompted: Boolean = false,
    val customWebViewUrl: String = "",
    val lastWebViewPath: String = "",
    val appLanguageTag: String = "",
    val forceShowCalendar: Boolean = false,
    val fakeUsUser: Boolean = false
)
