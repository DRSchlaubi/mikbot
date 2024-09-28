package dev.schlaubi.mikmusic.util

import dev.arbjerg.lavalink.protocol.v4.Track

val Track.spotifyId: String?
    get() = if(info.sourceName == "spotify") info.identifier else null
