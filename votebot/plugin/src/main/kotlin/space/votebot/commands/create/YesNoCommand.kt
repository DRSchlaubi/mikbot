package space.votebot.commands.create

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.entity.channel.Channel
import space.votebot.common.models.PollSettings
import space.votebot.common.models.StoredPollSettings
import space.votebot.core.VoteBotModule

class YesNoArguments : Arguments(), CreateSettings {
    override val title: String by voteTitle()
    override val answers: List<String> = listOf("Yes", "no")
    override val settings: PollSettings = StoredPollSettings(
        maxChanges = 0,
        maxVotes = 1
    )
    override val channel: Channel? by voteChannel()
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
