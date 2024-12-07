package dev.schlaubi.mikmusic.util

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikmusic.api.types.SimpleQueuedTrack

fun List<Track>.mapToQueuedTrack(user: UserBehavior) = map { SimpleQueuedTrack(it, user.id) }
