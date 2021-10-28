package dev.schlaubi.musicbot.module.music.playlist.commands

import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.musicbot.module.music.autocomplete.autoCompletedYouTubeQuery
import dev.schlaubi.musicbot.module.music.player.queue.QueueOptions
import dev.schlaubi.musicbot.module.music.player.queue.findTracks
import dev.schlaubi.musicbot.utils.database

class PlaylistAddArguments : PlaylistArguments(), QueueOptions {
    override val query by autoCompletedYouTubeQuery("The query to play")
    override val soundcloud by defaultingBoolean(
        "soundcloud",
        "Searches for this item on SoundCloud instead of YouTube",
        false
    )
    val search by defaultingBoolean("search", "Display multiple search results", false)
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

            database.playlists.save(playlist.copy(songs = playlist.songs + tracks))

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
