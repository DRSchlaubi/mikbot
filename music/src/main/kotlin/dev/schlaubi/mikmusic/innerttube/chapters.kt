package dev.schlaubi.mikmusic.innerttube

import kotlinx.serialization.Serializable

@Serializable
data class VideoRenderer(
    val videoId: String,
    val expandableMetadata: ExpandableMetadata? = null
)

@Serializable
data class ExpandableMetadata(val expandableMetadataRenderer: ExpandableMetadataRenderer)

@Serializable
data class ExpandableMetadataRenderer(val expandedContent: ExpandedContent)

@Serializable
data class ExpandedContent(val horizontalCardListRenderer: HorizontalCardListRenderer)

@Serializable
data class HorizontalCardListRenderer(
    val cards: List<Card>,
)

@Serializable
data class Card(
    val macroMarkersListItemRenderer: MacroMarkersListItemRenderer
)

@Serializable
data class MacroMarkersListItemRenderer(
    val title: TextBlock,
    val timeDescription: TextBlock,
)

@Serializable
data class TextBlock(val simpleText: String): CharSequence by simpleText {
    override fun toString(): String = simpleText
}




