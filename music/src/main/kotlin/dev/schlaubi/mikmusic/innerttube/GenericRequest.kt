package dev.schlaubi.mikmusic.innerttube

import kotlinx.serialization.Serializable

@Serializable
data class InnerTubeContext(val client: Client) {
    @Serializable
    data class Client(val clientName: String, val clientVersion: String)
}

interface InnerTubeRequest {
    val context: InnerTubeContext
}
