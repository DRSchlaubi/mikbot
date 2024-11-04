package dev.schlaubi.mikmusic.playlist.commands

import dev.kordex.core.commands.application.slash.converters.impl.optionalEnumChoice
import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.kord.rest.builder.message.embed
import dev.kordex.core.i18n.EMPTY_KEY
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.autocomplete.autoCompletedYouTubeQuery
import dev.schlaubi.mikmusic.player.queue.QueueOptions
import dev.schlaubi.mikmusic.player.queue.findTracks
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import dev.schlaubi.mikmusic.util.mapToEncoded

class PlaylistAddArguments : PlaylistArguments(), QueueOptions {
    override val query by autoCompletedYouTubeQuery(MusicTranslations.Commands.Playlist.Add.Arguments.Query.name, MusicTranslations.Commands.Playlist.Add.Arguments.Query.description)
    override val searchProvider: QueueOptions.SearchProvider? by optionalEnumChoice<QueueOptions.SearchProvider> {
        name = MusicTranslations.Commands.Playlist.Add.Arguments.SearchProvider.name
        description = MusicTranslations.Commands.Playlist.Add.Arguments.SearchProvider.description
        typeName = EMPTY_KEY
    }
    val search by defaultingBoolean {
        name = MusicTranslations.Commands.Playlist.Add.Arguments.Search.name
        description = MusicTranslations.Commands.Playlist.Add.Arguments.Search.description
        defaultValue = false
    }
    override val top: Boolean = false
    override val force: Boolean = false
    override val shuffle: Boolean? = null
    override val loop: Boolean? = null
    override val loopQueue: Boolean? = null

}

fun PlaylistModule.addCommand() = ephemeralSubCommand(::PlaylistAddArguments) {
    name = MusicTranslations.Commands.Playlist.Add.name
    description = MusicTranslations.Commands.Playlist.Add.description

    action {
        checkPermissions { playlist ->
            val result = findTracks(node, arguments.search) ?: return@action
            val tracks = result.tracks.mapToEncoded()

            PlaylistDatabase.collection.save(playlist.copy(songs = playlist.songs + tracks))

            respond {
                embed {
                    title = translate(MusicTranslations.Commands.Playlist.Add.added, tracks.size.toString())

                    with(result) {
                        addInfo(musicPlayer.link, this@action)
                    }
                }
            }
        }
    }
}
