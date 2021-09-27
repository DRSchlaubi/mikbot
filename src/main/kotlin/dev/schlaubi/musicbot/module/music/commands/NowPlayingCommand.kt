package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.checks.anyMusicPlaying
import dev.schlaubi.musicbot.utils.addSong

class NowPlayingArguments : Arguments() {
    val index by optionalInt("position", "The number of the song in the queue to display the info for")
}

suspend fun MusicModule.nowPlayingCommand() = publicSlashCommand(::NowPlayingArguments) {
    name = "now-playing"
    description = "Displays information about the currently playing track"

    check {
        anyMusicPlaying(this@nowPlayingCommand)
    }

    action {
        val index = arguments.index
        val (playingTrack) = if (index != null) {
            musicPlayer.queuedTracks.getOrNull(index) ?: run {
                respond { translate("commands.now_playing.invalid_index") }
                return@action
            }
        } else musicPlayer.playingTrack ?: return@action

        respond {
            embed {
                addSong(this@action, playingTrack)

                field {
                    name = translate("commands.now_playing.serving_node.discord")
                    value = "`${java.net.InetAddress.getLocalHost().hostName}`"
                }

                field {
                    name = translate("commands.now_playing.serving_node.music")
                    value = "`${link.node.host}`"
                }

                field {
                    name = translate("commands.now_playing.progress")
                    value = "${player.position}/${playingTrack.length}"
                }
            }
        }
    }
}
