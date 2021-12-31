package space.votebot.commands.vote.create

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.entity.channel.Channel
import space.votebot.command.PollSettingsArguments
import space.votebot.common.models.PollSettings
import space.votebot.core.VoteBotModule

private val optionsRegex = "\\s*\\|\\s*".toRegex()

class CreateOptions : Arguments(), CreateSettings, PollSettingsArguments {
    override val title: String by voteTitle()

    private val answersOptions by string("answers", "A pipe (|) seperated list of available options")
    override val answers: List<String> by lazy { answersOptions.split(optionsRegex) }

    override val channel: Channel? by voteChannel()

    override val maxVotes by maxVotes("How many times a user is allowed to vote")
    override val maxChanges by maxChanges("How many times a user is allowed to change their vote")
    override val hideResults: Boolean? by hideResults("Whether to show results only to people who voted or not")
    override val publicResults: Boolean? by publicResults("Whether to share who voted for what with the author or not")
    override val deleteAfterPeriod by voteDuration("Amount of time after which this poll should expire")
    override val showChartAfterClose: Boolean? by showChart("Whether to show a chart after the poll finished or not")
    override val emojiMode: PollSettings.EmojiMode? by emojiMode("How to use emojis in this poll")

    override val settings: PollSettings get() = this
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
