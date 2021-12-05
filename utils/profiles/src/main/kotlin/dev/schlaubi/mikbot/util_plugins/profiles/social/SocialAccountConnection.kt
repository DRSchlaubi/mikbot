package dev.schlaubi.mikbot.util_plugins.profiles.social

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class SocialAccountConnection(
    @SerialName("_id")
    @Contextual
    val id: Id<SocialAccountConnection> = newId(),
    val userId: Long,
    val type: SocialAccountConnectionType,
    val username: String,
    val url: String,
    val platformId: String
)
