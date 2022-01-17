package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikmusic.autocomplete.autoCompletedYouTubeQuery
import dev.schlaubi.mikmusic.player.queue.QueueOptions
import dev.schlaubi.mikmusic.player.queue.findTracks
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistAddArguments : PlaylistArguments(), QueueOptions {
    override val query by autoCompletedYouTubeQuery("The query to play")
    override val soundcloud by defaultingBoolean {
        name = "soundcloud"
        description = "Searches for this item on SoundCloud instead of YouTube"
        defaultValue = false
    }
    val search by defaultingBoolean {
        name = "search"
        description = "Display multiple search results"
        defaultValue = false
    }
    override val top: Boolean = false
    override val force: Boolean = false
}

fun PlaylistModule.addCommand() = ephemeralSubCommand(::PlaylistAddArguments) {
    name = "add"
    description = "Adds a new track to the Playlist"

    action {
        checkPermissions { playlist ->
            val result = findTracks(musicPlayer, arguments.search) ?: return@action
            val tracks = result.tracks

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
