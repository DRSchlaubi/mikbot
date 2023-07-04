package dev.schlaubi.mikmusic.util

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.mikmusic.player.SimpleQueuedTrack

fun List<Track>.mapToQueuedTrack(user: UserBehavior) = map { SimpleQueuedTrack(it, user.id) }
