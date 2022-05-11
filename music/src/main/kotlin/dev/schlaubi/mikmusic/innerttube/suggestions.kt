package dev.schlaubi.mikmusic.innerttube

import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionsSectionRendererContent(
    val searchSuggestionsSectionRenderer: InnerTubeBox<SearchSuggestionsRendererContent>,
)

@Serializable
data class SearchSuggestionsRendererContent(
    val searchSuggestionRenderer: SearchSuggestionsRenderer,
)

@Serializable
data class SearchSuggestionsRenderer(val suggestion: Suggestion) {
    @Serializable
    data class Suggestion(val runs: List<Run>) {
        @Serializable
        data class Run(val text: String, val bold: Boolean = false)

        fun joinRuns() = runs.joinToString("", transform = Run::text)
    }
}
