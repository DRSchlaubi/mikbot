package space.votebot.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.message
import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.Permission
import dev.schlaubi.mikbot.plugin.api.util.discordError
import space.votebot.common.models.Poll
import space.votebot.core.VoteBotDatabase
import space.votebot.core.findOneByMessage

abstract class PollArguments(pollArgumentDescription: String) : Arguments() {
    val pollMessage by message("poll", pollArgumentDescription)
}

suspend fun <A : PollArguments> SlashCommandContext<*, A>.poll(): Poll {
    val poll = VoteBotDatabase.polls.findOneByMessage(arguments.pollMessage)
        ?: discordError(translate("commands.generic.poll_bot_found"))
    val user = getUser()
    if (user.id.value != poll.authorId &&
        getMember()?.run { asMember().hasPermission(Permission.ManageGuild) } != true
    ) {
        discordError(translate("commands.generic.no_permission"))
    }

    return poll
}
