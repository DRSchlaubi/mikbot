package dev.schlaubi.mikbot.util_plugins.profiles.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyUser(
    @SerialName("display_name")
    override val displayName: String,
    override val id: String,
    @SerialName("external_urls")
    val externalUrls: Map<String, String>
) : User {
    override val url: String
        get() = externalUrls["spotify"]!!
}
