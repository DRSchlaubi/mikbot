package dev.schlaubi.mikmusic.innerttube

import dev.kord.common.Locale


suspend fun requestYouTubeAutoComplete(query: String, locale: Locale): List<String> {
    val response = InnerTubeClient.requestMusicAutoComplete(query, locale)

    return response.contents.firstOrNull()?.searchSuggestionsSectionRenderer?.contents?.mapNotNull {
        it.searchSuggestionRenderer?.suggestion?.joinRuns()
    } ?: emptyList()
}

