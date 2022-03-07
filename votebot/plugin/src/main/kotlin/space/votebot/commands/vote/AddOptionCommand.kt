package space.votebot.commands.vote

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import space.votebot.command.poll
import space.votebot.common.models.Poll
import space.votebot.core.VoteBotDatabase
import space.votebot.core.VoteBotModule
import space.votebot.core.recalculateEmojis
import space.votebot.core.updateMessages

class AddOptionArguments : Arguments() {
    val poll by poll {
        name = "poll"
        description = "The poll you want to add the argument to"
    }

    val option by string {
        name = "option"
        description = "The option you want to add"
    }
    val position by optionalInt {
        name = "position"
        description = "The position at which the option should be inserted"
    }
}

suspend fun VoteBotModule.addOptionCommand() = ephemeralSlashCommand(::AddOptionArguments) {
    name = "add-option"
    description = "Adds an option to a poll"

    action {
        val poll = arguments.poll
        if (arguments.option.length > 50) {
            discordError(translate("vote.create.too_long_option", arrayOf(arguments.option)))
        }

        val option = Poll.Option.ActualOption(
            arguments.position?.minus(1),
            arguments.option,
            null
        )

        val newPoll = poll.copy(options = poll.options + option).recalculateEmojis(safeGuild)

        VoteBotDatabase.polls.save(newPoll)
        newPoll.updateMessages(channel.kord, guild!!)
        respond {
            content = translate("commands.add_option.success", arrayOf(arguments.option))
        }
    }
}
