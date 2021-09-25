package dev.schlaubi.musicbot.module.owner.verification

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
@JvmRecord
data class Invitation(@SerialName("_id") @Contextual val id: Id<Invitation>, val guildId: Snowflake)
