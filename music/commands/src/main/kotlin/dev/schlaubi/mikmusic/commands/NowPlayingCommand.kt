package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.rest.builder.message.embed
import dev.schlaubi.mikmusic.checks.anyMusicPlaying
import dev.schlaubi.mikmusic.checks.musicQuizAntiCheat
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.util.addSong
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class NowPlayingArguments : Arguments() {
    val index by optionalInt {
        name = "position"
        description = "commands.now_playing.arguments.position.description"
    }
}

private val regex = """\.[0-9]*""".toRegex()

suspend fun MusicModule.nowPlayingCommand() = publicSlashCommand(::NowPlayingArguments) {
    name = "now-playing"
    description = "commands.now_playing.description"

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
                        "${player.positionDuration.toString().replace(regex, "")}/${
                            playingTrack.info.length.toDuration(
                                DurationUnit.MILLISECONDS
                            )
                        }"
                }
            }
        }
    }
}
