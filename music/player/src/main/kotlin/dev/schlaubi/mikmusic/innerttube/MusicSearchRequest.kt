package dev.schlaubi.mikmusic.innerttube

import kotlinx.serialization.Serializable

@Serializable
data class MusicSearchRequest(override val context: InnerTubeContext, val input: String) : InnerTubeRequest

@Serializable
data class SearchRequest(override val context: InnerTubeContext, val query: String) : InnerTubeRequest
