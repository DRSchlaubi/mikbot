package dev.schlaubi.mikbot.utils.roleselector

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

object RoleSelectorDatabase : KoinComponent {
    val autoRoleCollection = database.getCollection<AutoRole>("autoroles")
}

@Serializable
data class AutoRole(@SerialName("_id") val guildId: Snowflake, val roleId: Snowflake)