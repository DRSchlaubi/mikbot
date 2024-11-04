package dev.schlaubi.mikmusic.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalInt
import dev.kordex.core.extensions.publicSlashCommand
import dev.kord.rest.builder.message.embed
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.checks.anyMusicPlaying
import dev.schlaubi.mikmusic.checks.musicQuizAntiCheat
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.util.addSong
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class NowPlayingArguments : Arguments() {
    val index by optionalInt {
        name = MusicTranslations.Commands.Now_playing.Arguments.Position.name
        description = MusicTranslations.Commands.Now_playing.Arguments.Position.description
    }
}

private val regex = """\.[0-9]*""".toRegex()

suspend fun MusicModule.nowPlayingCommand() = publicSlashCommand(::NowPlayingArguments) {
    name = MusicTranslations.Commands.Now_playing.name
    description = MusicTranslations.Commands.Now_playing.description
    musicControlContexts()

    check {
        anyMusicPlaying(this@nowPlayingCommand)
        musicQuizAntiCheat(this@nowPlayingCommand)
    }

    action {
        val index = arguments.index
        val (playingTrack) = if (index != null) {
            musicPlayer.queuedTracks.getOrNull(index) ?: run {
                respond { translate(MusicTranslations.Commands.Now_playing.invalid_index) }
                return@action
            }
        } else musicPlayer.playingTrack ?: return@action

        respond {
            embed {
                addSong(this@action, playingTrack)

                field {
                    name = translate(MusicTranslations.Commands.Now_playing.Serving_node.discord)
                    value = "`${java.net.InetAddress.getLocalHost().hostName}`"
                }

                field {
                    name = translate(MusicTranslations.Commands.Now_playing.Serving_node.music)
                    value = "`${link.node.host}`"
                }

                field {
                    name = translate(MusicTranslations.Commands.Now_playing.progress)
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
