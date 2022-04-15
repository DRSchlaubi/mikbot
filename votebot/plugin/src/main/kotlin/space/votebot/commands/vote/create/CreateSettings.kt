package space.votebot.commands.vote.create

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.schlaubi.mikbot.plugin.api.util.discordError
import space.votebot.common.models.PollSettings

interface CreateSettings {
    val title: String
    val answers: List<String>
    val settings: PollSettings
    val channel: Channel?

    fun Arguments.voteChannel() = optionalChannel {
        name = "channel"
        description = "The channel to send the poll in"

        validate {
            if (value != null && value?.asChannelOfOrNull<TopGuildMessageChannel>() == null) {
                discordError(translate("commands.create.invalid_channel"))
            }
        }
    }

    fun Arguments.voteTitle() = string {
        name = "title"
        description = "The title of the vote"
    }
}
