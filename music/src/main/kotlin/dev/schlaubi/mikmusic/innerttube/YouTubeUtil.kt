package dev.schlaubi.mikmusic.innerttube

import dev.kord.common.Locale
import dev.schlaubi.mikmusic.player.Chapter
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val timeSeparator = ':'

suspend fun requestYouTubeAutoComplete(query: String, locale: Locale): List<String> {
    val response = InnerTubeClient.requestMusicAutoComplete(query, locale)

    return response.firstOrNull()?.searchSuggestionsSectionRenderer?.mapNotNull {
        it.searchSuggestionRenderer?.suggestion?.joinRuns()
    } ?: emptyList()
}

suspend fun requestVideoRendererById(id: String): VideoRenderer? {
    val response = InnerTubeClient.requestVideoSearch(id)

    return response
        .contents
        .twoColumnSearchResultsRenderer
        .primaryContents
        .sectionListRenderer
        .contents
        .asSequence()
        .flatMap {
            it.itemSectionRenderer?.contents?.map(VideoRendererConsent::videoRenderer) ?: emptyList()
        }
        .filterNotNull()
        .firstOrNull {
            it.videoId == id
        }
}

suspend fun requestVideoChaptersById(id: String): List<Chapter> {
    val video = requestVideoRendererById(id) ?: return emptyList()

    val content = video.expandableMetadata?.expandableMetadataRenderer?.expandedContent ?: return emptyList()

    return content.horizontalCardListRenderer.cards.map {
        val renderer = it.macroMarkersListItemRenderer

        Chapter(renderer.timeDescription.simpleText.parseDuration(), renderer.title.simpleText)
    }
}

private fun String.parseDuration(): Duration {
    val units = split(timeSeparator)
    val unitCount = units.size - 1
    val multiplierOffset = if (unitCount > 2) 1 else 0

    val seconds = units.foldRightIndexed(0) { index, input, acc ->
        val multiplier = 60.0.pow(multiplierOffset + (unitCount - index)).toInt()
        val parsed = input.trimEnd().toInt() * multiplier

        acc + parsed
    }

    return seconds.seconds
}

