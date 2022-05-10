package space.votebot.commands.vote.create

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import dev.kord.common.entity.ChannelType
import dev.kord.core.entity.channel.Channel
import space.votebot.common.models.PollSettings

interface CreateSettings {
    val title: String
    val answers: List<String>
    val settings: PollSettings
    val channel: Channel?

    fun Arguments.voteChannel() = optionalChannel {
        name = "channel"
        description = "generic.create_arguments.channel"

        requiredChannelTypes.add(ChannelType.GuildText)
    }

    fun Arguments.voteTitle() = string {
        name = "title"
        description = "generic.create_arguments.title"
    }
}
