package space.votebot.common.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class DiscordUser(
    val id: ULong,
    val username: String,
    @SerialName("display_name")
    val displayName: String?,
    val discriminator: String,
    val avatar: String?,
    val avatarUrl: String = cdnUrl(id, avatar, discriminator),
)

private fun cdnUrl(id: ULong, hash: String?, discriminator: String) = if (hash == null) {
    "https://cdn.discordapp.com/embed/avatars/$discriminator.png"
} else {
    "https://cdn.discordapp.com/avatars/$id/$hash.${if (hash.startsWith("a_")) ".gif" else ".png"}"
}
