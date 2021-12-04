package dev.schlaubi.mikbot.util_plugins.profiles.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitLabUser(
    @SerialName("id")
    val platformId: Int,
    @SerialName("web_url")
    val webUrl: String,
    val username: String
) : User {
    override val id: String
        get() = platformId.toString()
    override val url: String
        get() = webUrl
    override val displayName: String
        get() = username
}
