package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

@JvmRecord
@Serializable
data class BotUser(
    @SerialName("_id")
    val id: Snowflake,
    @Contextual
    val language: Locale = SupportedLocales.ENGLISH,
    val defaultSchedulerSettings: SchedulerSettings? = null,
    val unoStats: UnoStats? = null
)

@Serializable
@JvmRecord
data class UnoStats(
    val wins: Int,
    val losses: Int,
    val ratio: Double
)
