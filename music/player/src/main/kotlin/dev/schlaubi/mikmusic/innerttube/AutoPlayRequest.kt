package dev.schlaubi.mikmusic.innerttube

import kotlinx.serialization.Serializable

@Serializable
data class AutoPlayRequest(
    val videoId: String,
    val playlistId: String? = null,
    val params: String? = null,
    override val context: InnerTubeContext,
) : InnerTubeRequest
