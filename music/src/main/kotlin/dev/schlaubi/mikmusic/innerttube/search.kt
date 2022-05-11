package dev.schlaubi.mikmusic.innerttube

import kotlinx.serialization.Serializable

@Serializable
data class TwoColumnSearchResultsRendererContent(
    val twoColumnSearchResultsRenderer: TwoColumnSearchResultsRenderer,
)

@Serializable
data class TwoColumnSearchResultsRenderer(
    val primaryContents: SectionListRendererContent,
)

@Serializable
data class SectionListRendererContent(
    val sectionListRenderer: InnerTubeBox<ItemSectionRendererContent>,
)

@Serializable
data class ItemSectionRendererContent(
    val itemSectionRenderer: InnerTubeBox<VideoRendererConsent>? = null,
)

@Serializable
data class VideoRendererConsent(val videoRenderer: VideoRenderer? = null)
