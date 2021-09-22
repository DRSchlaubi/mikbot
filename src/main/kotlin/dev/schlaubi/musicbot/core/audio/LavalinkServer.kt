package dev.schlaubi.musicbot.core.audio

import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class LavalinkServer(val url: String, val password: String)
