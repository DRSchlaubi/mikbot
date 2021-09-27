package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.checks.anyMusicPlaying
import dev.schlaubi.musicbot.utils.addSong

suspend fun MusicModule.nowPlayingCommand() = publicSlashCommand {
    name = "now-playing"
    description = "Displays information about the currently playing track"

    check {
        anyMusicPlaying(this@nowPlayingCommand)
    }

    action {
        val playingTrack = player.playingTrack ?: return@action

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
