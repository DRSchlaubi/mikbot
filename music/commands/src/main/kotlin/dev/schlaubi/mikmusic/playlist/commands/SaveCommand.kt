package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.player.QueuedTrack
import dev.schlaubi.mikmusic.player.queue.QueueOptions
import dev.schlaubi.mikmusic.player.queue.findTracks
import dev.schlaubi.mikmusic.playlist.Playlist
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import dev.schlaubi.mikmusic.playlist.mapToEncoded
import org.litote.kmongo.newId

class PlaylistSaveArguments : Arguments(), QueueOptions {
    val name by string {
        name = "name"
        description = "commands.playlist.save.arguments.name.description"
    }
    val public by defaultingBoolean {
        name = "public"
        description = "commands.playlist.save.arguments.public.description"
        defaultValue = false
    }
    val importFrom by optionalString {
        name = "import_from"
        description = "commands.playlist.save.arguments.import_from.description"
    }
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
                tracks.mapToEncoded(),
                arguments.public
            )

            PlaylistDatabase.collection.save(playlist)

            respond {
                content = translate("commands.playlist.save.saved", arrayOf(arguments.name, tracks.size))
            }
        }
    }
}
