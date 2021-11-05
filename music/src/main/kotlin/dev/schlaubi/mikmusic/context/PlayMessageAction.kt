package dev.schlaubi.mikmusic.context

import com.kotlindiscord.kord.extensions.commands.application.message.EphemeralMessageCommandContext
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.queue.QueueOptions
import dev.schlaubi.mikmusic.player.queue.queueTracks
import dev.schlaubi.mikbot.plugin.api.util.attachmentOrContentQuery

const val playActionName = "Play as track"

suspend fun MusicModule.playMessageAction() = ephemeralMessageCommand {
    name = playActionName

    check {
        joinSameChannelCheck(bot)
    }

    action {
        val query = event.interaction.messages.values.first().attachmentOrContentQuery

        val arguments = PlayMessageActionArguments(query)

        queue(arguments, musicPlayer)
    }
}

class PlayMessageActionArguments(override val query: String) : QueueOptions {
    override val force: Boolean = false
    override val top: Boolean = false
    override val soundcloud: Boolean = false
}

private suspend fun EphemeralMessageCommandContext.queue(
    arguments: PlayMessageActionArguments,
    musicPlayer: MusicPlayer
) = queueTracks(musicPlayer, true, arguments, { respond { it() } }) {
    editingPaginator { it() }
}
