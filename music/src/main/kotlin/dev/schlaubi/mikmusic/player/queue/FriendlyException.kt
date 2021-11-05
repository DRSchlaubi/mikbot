package dev.schlaubi.mikmusic.player.queue

import dev.schlaubi.lavakord.rest.TrackResponse

class FriendlyException(severity: TrackResponse.Error.Severity, message: String) :
    RuntimeException("$severity: $message")
