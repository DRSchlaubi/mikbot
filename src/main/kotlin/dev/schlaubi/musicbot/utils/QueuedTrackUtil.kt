package dev.schlaubi.musicbot.utils

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.musicbot.module.music.player.SimpleQueuedTrack

fun List<Track>.mapToQueuedTrack(user: UserBehavior) = map { SimpleQueuedTrack(it, user.id) }
