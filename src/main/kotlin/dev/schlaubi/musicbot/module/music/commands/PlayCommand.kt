package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.kord.core.behavior.edit
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.checks.joinSameChannelCheck
import dev.schlaubi.musicbot.module.music.player.queue.QueueArguments
import dev.schlaubi.musicbot.module.music.player.queue.findTracks
import dev.schlaubi.musicbot.utils.safeGuild

class PlayArguments : QueueArguments() {
    val search by defaultingBoolean("search", "Shows multiple search options", false)
}

suspend fun MusicModule.playCommand() {
    ephemeralSlashCommand(::PlayArguments) {
        name = "play"
        description = "Plays a song"

        check {
            joinSameChannelCheck(bot)
        }

        action {
            val voiceState = user.asMember(guild!!.id).getVoiceState()

            val channelId = voiceState.channelId!!
            link.connectAudio(channelId)

            safeGuild.getMember(this@playCommand.kord.selfId).edit {
                deafened = true
            }

            findTracks(musicPlayer, arguments.search)
        }
    }
}
