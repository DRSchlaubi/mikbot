package dev.schlaubi.mikmusic.core.audio

import kotlinx.serialization.Serializable

@Serializable
data class LavalinkServer(val url: String, val password: String)
