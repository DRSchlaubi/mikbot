package dev.schlaubi.mikmusic.context

import com.kotlindiscord.kord.extensions.commands.application.message.EphemeralMessageCommandContext
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import dev.schlaubi.mikbot.plugin.api.util.attachmentOrContentQuery
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.queue.QueueOptions
import dev.schlaubi.mikmusic.player.queue.SearchQuery
import dev.schlaubi.mikmusic.player.queue.queueTracks

const val playActionName = "Play as track"

suspend fun MusicModule.playMessageAction() = ephemeralMessageCommand {
    name = playActionName

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
