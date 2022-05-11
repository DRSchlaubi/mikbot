package dev.schlaubi.mikmusic.innerttube

import kotlinx.serialization.Serializable

@Serializable
data class InnerTubeBox<T>(val contents: List<T>) : List<T> by contents


@Serializable
data class InnerTubeSingleBox<T>(val contents: T)
