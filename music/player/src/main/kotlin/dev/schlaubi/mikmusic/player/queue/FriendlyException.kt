package dev.schlaubi.mikmusic.player.queue

import dev.arbjerg.lavalink.protocol.v4.Exception

class FriendlyException(severity: Exception.Severity, message: String?) :
    RuntimeException("$severity: $message")
