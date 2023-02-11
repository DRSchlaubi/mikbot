package space.votebot.common.models

import kotlinx.serialization.Serializable

@Serializable
public data class Server(
    val id: String,
    val name: String,
    val pollCount: Int,
    val icon: String?,
    val iconUrl: String? = icon?.let { cdnUrl(id, it) },
    val polls: List<PartialAPIPoll>? = null
)

private fun cdnUrl(id: String, hash: String) =
    "https://cdn.discordapp.com/icons/$id/$hash.${if (hash.startsWith("a_")) "gif" else "png"}"
