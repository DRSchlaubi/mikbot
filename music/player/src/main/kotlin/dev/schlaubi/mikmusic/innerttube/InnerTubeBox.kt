package dev.schlaubi.mikmusic.innerttube

import kotlinx.serialization.Serializable

@Serializable
data class InnerTubeBox<T>(val contents: List<T> = emptyList())


@Serializable
data class InnerTubeSingleBox<T>(val contents: T)
