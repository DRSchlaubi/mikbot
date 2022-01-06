package space.votebot.commands.vote.create

import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.entity.channel.Channel
import space.votebot.command.AbstractPollSettingsArguments
import space.votebot.common.models.PollSettings
import space.votebot.core.VoteBotModule

private val optionsRegex = "\\s*\\|\\s*".toRegex()

// For the weird order and reverse(): https://github.com/Kord-Extensions/kord-extensions/issues/123
class CreateOptions : AbstractPollSettingsArguments(), CreateSettings {
    override val channel: Channel? by voteChannel()

    private val answersOptions by string("answers", "A pipe (|) seperated list of available options")
    override val title: String by voteTitle()

    override val answers: List<String> by lazy { answersOptions.split(optionsRegex) }

    override val settings: PollSettings get() = this

    init {
        args.reverse()
    }
}

suspend fun VoteBotModule.createCommand() = ephemeralSlashCommand(::CreateOptions) {
    name = "create-vote"
    description = "Creates a new vote"

    action {
        createVote()
        respond {
            content = translate("commands.create.success", arrayOf(arguments.title))
        }
    }
}
