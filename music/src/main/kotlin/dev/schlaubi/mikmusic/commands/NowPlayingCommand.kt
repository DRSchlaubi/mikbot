package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikmusic.checks.anyMusicPlaying
import dev.schlaubi.mikmusic.checks.musicQuizAntiCheat
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.util.addSong

class NowPlayingArguments : Arguments() {
    val index by optionalInt {
        name = "position"
        description = "The number of the song in the queue to display the info for"
    }
}

private val regex = """\.[0-9]*""".toRegex()

suspend fun MusicModule.nowPlayingCommand() = publicSlashCommand(::NowPlayingArguments) {
    name = "now-playing"
    description = "Displays information about the currently playing track"

    check {
        anyMusicPlaying(this@nowPlayingCommand)
        musicQuizAntiCheat(this@nowPlayingCommand)
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
                    value =
                        "${player.positionDuration.toString().replace(regex, "")}/${playingTrack.length}"
                }
            }
        }
    }
}
