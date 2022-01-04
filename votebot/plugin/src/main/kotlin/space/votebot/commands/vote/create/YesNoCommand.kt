package space.votebot.commands.vote.create

import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.entity.channel.Channel
import space.votebot.command.AbstractPollSettingsArguments
import space.votebot.common.models.PollSettings
import space.votebot.common.models.StoredPollSettings
import space.votebot.core.VoteBotModule

class YesNoArguments : AbstractPollSettingsArguments(), CreateSettings {
    override val title: String by voteTitle()
    override val settings: PollSettings = StoredPollSettings(
        maxChanges = 0,
        maxVotes = 1
    )
    override val channel: Channel? by voteChannel()
    private val yesWord by defaultingString("yes-word", "The word you want for the 'Yes' option", "Yes")
    private val noWord by defaultingString("no-word", "The word you want for the 'Yes' option", "No")
    override val answers: List<String> by lazy { listOf(yesWord, noWord) }
}

suspend fun VoteBotModule.yesNowCommand() = ephemeralSlashCommand(::YesNoArguments) {
    name = "yes-no"
    description = "Allows you asking a simple yes no question"

    action {
        createVote()
        respond {
            content = translate("commands.yes_no.success")
        }
    }
}
