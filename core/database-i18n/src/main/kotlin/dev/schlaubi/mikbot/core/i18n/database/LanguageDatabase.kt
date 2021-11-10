package dev.schlaubi.mikbot.core.i18n.database

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import java.util.*

object LanguageDatabase : KoinComponent {
    val collection = database.getCollection<LangaugeUser>("language_users")
}


@Serializable
data class LangaugeUser(@SerialName("_id") val id: Snowflake, @Contextual val locale: Locale)
