package dev.schlaubi.mikbot.util_plugins.birthdays.database

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

object BirthdayDatabase : KoinComponent {
    val birthdays = database.getCollection<UserBirthday>("birthdays")
}

@Serializable
data class UserBirthday(@SerialName("_id") val id: Snowflake, val time: Instant, val timeZone: TimeZone)
