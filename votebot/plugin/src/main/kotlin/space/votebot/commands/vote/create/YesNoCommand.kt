package space.votebot.commands.vote.create

import com.kotlindiscord.kord.extensions.checks.isNotInThread
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.entity.channel.Channel
import space.votebot.command.AbstractPollSettingsArguments
import space.votebot.common.models.PollSettings
import space.votebot.core.VoteBotModule

class YesNoArguments : AbstractPollSettingsArguments(), CreateSettings {
    override val settings: PollSettings = this
    override val channel: Channel? by voteChannel()
    private val yesWord by defaultingString {
        name = "yes-word"
        description = "commands.yes_no.arguments.yes_word.description"
        defaultValue = "Yes"
    }
    private val noWord by defaultingString {
        name = "no-word"
        description = "commands.yes_no.arguments.no_word.description"
        defaultValue = "No"
    }
    override val answers: List<String> by lazy { listOf(yesWord, noWord) }
    override val title: String by voteTitle()

    init {
        // https://github.com/Kord-Extensions/kord-extensions/issues/123
        args.reverse()
    }
}

suspend fun VoteBotModule.yesNowCommand() = ephemeralSlashCommand(::YesNoArguments) {
    name = "yes-no"
    description = "commands.yes_no.description"

    check {
        isNotInThread()
    }

    action {
        createVote()
        respond {
            content = translate("commands.yes_no.success")
        }
    }
}
