package space.votebot.commands.create

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.entity.channel.Channel
import space.votebot.common.models.PollSettings
import space.votebot.common.models.StoredPollSettings
import space.votebot.core.VoteBotModule

class CreateOptions : Arguments(), CreateSettings {
    override val title: String by voteTitle()

    private val answersOptions by string("answers", "The options for the poll")
    override val answers: List<String> by lazy { answersOptions.split('|') }

    override val channel: Channel? by voteChannel()

    private val maxVotes by optionalInt("max-votes", "How many times a user is allowed to vote")
    private val maxChanges by optionalInt("max-changes", "How many times a user is allowed to change their vote")

    override val settings: PollSettings by lazy {
        StoredPollSettings(
            maxVotes = maxVotes, maxChanges = maxChanges
        )
    }
}

suspend fun VoteBotModule.createCommand() = ephemeralSlashCommand(::CreateOptions) {
    name = "create-vote"
    description = "Creates a new vote"

    action {
        createVote()
        respond {
            content = translate("commands.create.success", arguments.title)
        }
    }
}
