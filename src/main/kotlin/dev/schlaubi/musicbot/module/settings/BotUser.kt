package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Locale

@JvmRecord
@Serializable
data class BotUser(
    @Contextual
    val language: Locale = SupportedLocales.ENGLISH
)
