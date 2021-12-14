package dev.schlaubi.mikbot.util_plugins.profiles.social

import io.ktor.http.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class TwitterData<T>(val data: T)

@Serializable
data class TwitterUser @OptIn(ExperimentalSerializationApi::class) constructor(
    @JsonNames("user_id", "id") val userId: String,
    @JsonNames("screen_name", "username") val screenName: String,
) : User {
    override val id: String
        get() = userId
    override val url: String
        get() = "https://twitter.com/$screenName"
    override val displayName: String
        get() = screenName

    companion object {
        val requestUserEndpoint = Url("https://api.twitter.com/2/users/")
    }
}
