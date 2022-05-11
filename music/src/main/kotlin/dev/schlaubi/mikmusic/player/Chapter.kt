package dev.schlaubi.mikmusic.player

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class Chapter(
    @Contextual
    val startTime: Duration,
    val title: String,
)
