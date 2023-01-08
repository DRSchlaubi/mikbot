package dev.schlaubi.mikmusic.innerttube

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NextSongsResponse(
    val contents: SingleColumnMusicWatchNextResultsRendererContent,
)

@Serializable
data class SingleColumnMusicWatchNextResultsRendererContent(
    val singleColumnMusicWatchNextResultsRenderer: SingleColumnMusicWatchNextResultsRenderer,
)

@Serializable
data class SingleColumnMusicWatchNextResultsRenderer(
    val tabbedRenderer: TabbedRenderer,
)

@Serializable
data class TabbedRenderer(
    val watchNextTabbedResultsRenderer: WatchNextTabbedResultsRenderer,
)

@Serializable
data class WatchNextTabbedResultsRenderer(
    val tabs: List<Tab>,
)

@Serializable
data class Tab(
    val tabRenderer: TabRenderer,
)

@Serializable
data class TabRenderer(
    val title: String,
    val content: Content? = null,
) {
    @Serializable
    data class Content(val musicQueueRenderer: MusicQueueRenderer? = null)
}

@Serializable
data class MusicQueueRenderer(
    val content: Content,
) {
    @Serializable
    data class Content(
        val playlistPanelRenderer: PlaylistPanelRenderer,
    )
}

@Serializable
data class PlaylistPanelRenderer(val contents: List<PlaylistPanelVideoRendererContent>)

@Serializable
data class PlaylistPanelVideoRendererContent(
    val playlistPanelVideoWrapperRenderer: PlaylistPanelVideoWrapperRenderer? = null
)

@Serializable
data class PlaylistPanelVideoWrapperRenderer(val primaryRenderer: PlaylistPanelVideoRenderer)

@Serializable
data class PlaylistPanelVideoRenderer(
    val title: Runs<Text>,
    val selected: Boolean,
    val navigationEndpoints: NavigationEndpoints? = null,
    val videoId: String,
    @SerialName("longBylineText")
    val longByLineText: Runs<Text>? = null
) {
    @Serializable
    data class NavigationEndpoints(
        val watchEndpoint: WatchEndpoint,
    ) {
        @Serializable
        data class WatchEndpoint(
            val videoId: String,
            val playlistId: String,
            val index: Int,
            val params: String,
            val playlistSetVideoId: String,
        )
    }
}

@Serializable
data class Text(val text: String) : CharSequence by text {
    override fun toString(): String = text
}

@Serializable
data class Runs<T>(val runs: List<T>)
