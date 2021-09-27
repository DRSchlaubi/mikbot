package dev.schlaubi.musicbot.module.music.context

import com.kotlindiscord.kord.extensions.commands.application.message.EphemeralMessageCommandContext
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.checks.joinSameChannelCheck
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.module.music.player.queue.QueueOptions
import dev.schlaubi.musicbot.module.music.player.queue.queueTracks
import dev.schlaubi.musicbot.utils.attachmentOrContentQuery

const val playActionName = "Play as track"

suspend fun MusicModule.playMessageAction() = ephemeralMessageCommand {
    name = playActionName

    check {
        joinSameChannelCheck(bot)
    }

    action {
        val query = event.interaction.messages!!.values.first().attachmentOrContentQuery

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
