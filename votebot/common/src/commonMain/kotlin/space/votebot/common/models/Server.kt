package space.votebot.common.models

import kotlinx.serialization.Serializable

@Serializable
public data class Server(
    val id: ULong,
    val name: String,
    val pollCount: Int,
    val icon: String?,
    val url: String? = icon?.let { cdnUrl(id, it) },
    val polls: List<PartialAPIPoll>? = null
)

private fun cdnUrl(id: ULong, hash: String) =
    "https://cdn.discordapp.com/icons/$id/$hash.${if (hash.startsWith("a_")) ".gif" else ".png"}"
