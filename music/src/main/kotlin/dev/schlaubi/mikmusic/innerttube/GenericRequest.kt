package dev.schlaubi.mikmusic.innerttube

import kotlinx.serialization.Serializable

@Serializable
data class InnerTubeContext(val client: Client) {
    @Serializable
    data class Client(val clientName: String, val clientVersion: String, val hl: String = "en", val gl: String = "US")
}

interface InnerTubeRequest {
    val context: InnerTubeContext
}
