package dev.schlaubi.mikmusic.player.queue

import dev.schlaubi.lavakord.Exception


class FriendlyException(severity: Exception.Severity, message: String) :
    RuntimeException("$severity: $message")
