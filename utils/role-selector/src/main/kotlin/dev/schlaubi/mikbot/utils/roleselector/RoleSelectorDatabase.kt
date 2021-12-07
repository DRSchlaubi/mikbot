package dev.schlaubi.mikbot.utils.roleselector

import dev.kord.common.Color
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

object RoleSelectorDatabase : KoinComponent {
    val autoRoleCollection = database.getCollection<AutoRole>("autoroles")
    val roleSelectionCollection = database.getCollection<RoleSelectionMessage>("role_selections")
}

@Serializable
data class AutoRole(@SerialName("_id") val guildId: Snowflake, val roleId: Snowflake)

@Serializable
data class RoleSelectionMessage(
    @SerialName("_id") val messageId: Snowflake,
    val title: String,
    val description: String?,
    val embedColor: Color?,
    val roleSelections: List<RoleSelectionButton>
)

@Serializable
data class RoleSelectionButton(
    val buttonId: String,
    val label: String,
    val emoji: DiscordPartialEmoji?,
    val roleId: Snowflake
)
