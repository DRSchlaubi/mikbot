package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikmusic.autocomplete.autoCompletedYouTubeQuery
import dev.schlaubi.mikmusic.player.queue.QueueOptions
import dev.schlaubi.mikmusic.player.queue.findTracks
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import dev.schlaubi.mikmusic.playlist.mapToEncoded

class PlaylistAddArguments : PlaylistArguments(), QueueOptions {
    override val query by autoCompletedYouTubeQuery("commands.playlist.add.arguments.query.description")
    override val soundcloud by defaultingBoolean {
        name = "soundcloud"
        description = "commands.playlist.add.arguments.soundscloud.description"
        defaultValue = false
    }
    val search by defaultingBoolean {
        name = "search"
        description = "commands.playlist.add.arguments.search.description"
        defaultValue = false
    }
    override val top: Boolean = false
    override val force: Boolean = false
}

fun PlaylistModule.addCommand() = ephemeralSubCommand(::PlaylistAddArguments) {
    name = "add"
    description = "commands.playlist.add.description"

    action {
        checkPermissions { playlist ->
            val result = findTracks(musicPlayer, arguments.search) ?: return@action
            val tracks = result.tracks.mapToEncoded()

            PlaylistDatabase.collection.save(playlist.copy(songs = playlist.songs + tracks))

            respond {
                embed {
                    title = translate("commands.playlist.add.added", arrayOf(tracks.size.toString()))

                    with(result) {
                        addInfo(musicPlayer.link, this@action)
                    }
                }
            }
        }
    }
}
