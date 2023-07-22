package dev.schlaubi.mikmusic.innerttube

import dev.kord.common.Locale
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Entry(
    val text: String,
    val thumbnail: String? = null,
    val url: String? = null,
)

private fun MusicResponsiveListItemRenderer.NavigationEndpoint.toUrl() = when {
    browseEndpoint != null -> "https://music.youtube.com/channel/${browseEndpoint.browseId}"
    watchEndpoint != null -> "https://music.youtube.com/watch?v${watchEndpoint.videoId}"
    else -> error("Unknown endpoint: $this")
}

suspend fun main() {
    val results = InnerTubeClient.requestMusicAutoComplete("a", Locale.GERMAN)

    val result = results.contents.flatMap {
        it.searchSuggestionsSectionRenderer.contents.mapNotNull {
            if (it.searchSuggestionRenderer != null) {
                Entry(it.searchSuggestionRenderer.suggestion.joinRuns())
            } else if (it.musicResponsiveListItemRenderer?.navigationEndpoint != null
                && it.musicResponsiveListItemRenderer.thumbnail != null
            ) {
                val item = it.musicResponsiveListItemRenderer
                Entry(
                    item.flexColumns.first().musicResponsiveListItemFlexColumnRenderer.text.joinRuns(),
                    item.thumbnail!!.musicThumbnailRenderer.thumbnail.thumbnails.first().url,
                    item.navigationEndpoint!!.toUrl()
                )
            } else {
                null
            }
        }
    }
    println(Json { prettyPrint = true }.encodeToString(result))
}
