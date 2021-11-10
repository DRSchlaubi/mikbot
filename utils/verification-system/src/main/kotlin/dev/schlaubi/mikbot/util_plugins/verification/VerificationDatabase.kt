package dev.schlaubi.mikbot.util_plugins.verification

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

object VerificationDatabase : KoinComponent {
    val collection = database.getCollection<VerificationListEntry>("verified_guilds")
    val invites = database.getCollection<Invitation>("invites")
}


@Serializable
data class VerificationListEntry(@SerialName("_id") val guildId: Snowflake, val verified: Boolean)
