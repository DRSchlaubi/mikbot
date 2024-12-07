package dev.schlaubi.mikmusic.playlist.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.kordex.core.commands.converters.impl.optionalString
import dev.kordex.core.commands.converters.impl.string
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.api.types.QueuedTrack
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.queue.QueueOptions
import dev.schlaubi.mikmusic.player.queue.findTracks
import dev.schlaubi.mikmusic.playlist.Playlist
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import dev.schlaubi.mikmusic.util.mapToEncoded
import org.litote.kmongo.newId

class PlaylistSaveArguments : Arguments(), QueueOptions {
    val name by string {
        name = MusicTranslations.Commands.Playlist.Save.Arguments.Name.name
        description = MusicTranslations.Commands.Playlist.Save.Arguments.Name.description
    }
    val public by defaultingBoolean {
        name = MusicTranslations.Commands.Playlist.Save.Arguments.Public.name
        description = MusicTranslations.Commands.Playlist.Save.Arguments.Public.description
        defaultValue = false
    }
    val importFrom by optionalString {
        name = MusicTranslations.Commands.Playlist.Save.Arguments.ImportFrom.name
        description = MusicTranslations.Commands.Playlist.Save.Arguments.ImportFrom.description
    }
    override val query: String
        get() = importFrom ?: error("Cannot find tracks if importFrom is not specified")
    override val force: Boolean = false
    override val top: Boolean = false
    override val searchProvider: QueueOptions.SearchProvider? = null
    override val shuffle: Boolean? = null
    override val loop: Boolean? = null
    override val loopQueue: Boolean? = null
}

fun PlaylistModule.saveCommand() = ephemeralSubCommand(::PlaylistSaveArguments) {
    name = MusicTranslations.Commands.Playlist.Save.name
    description = MusicTranslations.Commands.Playlist.Save.description

    musicControlContexts()

    action {
        if (musicPlayer.playingTrack == null && arguments.importFrom == null) {
            respond {
                content = translate(MusicTranslations.Music.Checks.notPlaying)
            }
            return@action
        }

        checkName(arguments.name, arguments.public) {
            val tracks = if (arguments.importFrom == null) {
                (listOfNotNull(musicPlayer.playingTrack) + musicPlayer.queuedTracks).map(QueuedTrack::track)
            } else {
                findTracks(node, false)?.tracks ?: return@action
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
                content = translate(MusicTranslations.Commands.Playlist.Save.saved, arguments.name, tracks.size)
            }
        }
    }
}
