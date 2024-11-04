package dev.schlaubi.mikmusic.context

import dev.kordex.core.commands.application.message.EphemeralMessageCommandContext
import dev.kordex.core.extensions.ephemeralMessageCommand
import dev.schlaubi.mikbot.plugin.api.util.attachmentOrContentQuery
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.queue.SearchQuery
import dev.schlaubi.mikmusic.player.queue.queueTracks


suspend fun MusicModule.playMessageAction() = ephemeralMessageCommand {
    name = MusicTranslations.Context.Message.play_as_track

    musicControlContexts()

    check {
        joinSameChannelCheck(bot)
    }

    action {
        val query = event.interaction.messages.values.first().attachmentOrContentQuery

        val arguments = SearchQuery(query)

        queue(arguments, musicPlayer)
    }
}

private suspend fun EphemeralMessageCommandContext<*>.queue(
    arguments: SearchQuery,
    musicPlayer: MusicPlayer
) = queueTracks(musicPlayer, true, arguments, { respond { it() } }) {
    editingPaginator { it() }
}
