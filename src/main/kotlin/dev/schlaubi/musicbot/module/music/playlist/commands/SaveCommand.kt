package dev.schlaubi.musicbot.module.music.playlist.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.player.QueuedTrack
import dev.schlaubi.musicbot.module.music.player.queue.QueueOptions
import dev.schlaubi.musicbot.module.music.player.queue.findTracks
import dev.schlaubi.musicbot.module.music.playlist.Playlist
import dev.schlaubi.musicbot.utils.database
import org.litote.kmongo.newId

class PlaylistSaveArguments : Arguments(), QueueOptions {
    val name by string("name", "The name of the playlist")
    val public by defaultingBoolean("public", "Whether this playlist is supposed to be public or not", false)
    val importFrom by optionalString(
        "import_from",
        "Link to a Track source (YouTube Video/Playlist, Spotify Song/Playlist/Album) to import"
    )
    override val query: String
        get() = importFrom ?: error("Cannot find tracks if importFrom is not specified")
    override val force: Boolean = false
    override val top: Boolean = false
    override val soundcloud: Boolean = false
}

fun PlaylistModule.saveCommand() = ephemeralSubCommand(::PlaylistSaveArguments) {
    name = "create"
    description = "Creates a new playlist"

    action {
        if (musicPlayer.playingTrack == null && arguments.importFrom == null) {
            respond {
                content = translate("music.checks.not_playing")
            }
        }

        checkName(arguments.name, arguments.public) {
            val tracks = if (arguments.importFrom == null) {
                (listOfNotNull(musicPlayer.playingTrack) + musicPlayer.queuedTracks).map(QueuedTrack::track)
            } else {
                findTracks(musicPlayer, false)?.tracks ?: return@action
            }

            val playlist = Playlist(
                newId(),
                user.id,
                arguments.name,
                tracks,
                arguments.public
            )

            database.playlists.save(playlist)

            respond {
                content = translate("commands.playlist.save.saved", arrayOf(arguments.name, tracks.size))
            }
        }
    }
}
