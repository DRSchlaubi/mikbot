package space.votebot.commands.create

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalDuration
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import dev.kord.common.entity.ChannelType
import dev.kord.core.entity.channel.Channel
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.toDuration
import space.votebot.common.models.PollSettings
import kotlin.time.Duration

interface CreateSettings {
    val title: String
    val answers: List<String>
    val settings: PollSettings
    val channel: Channel?

    fun Arguments.voteChannel() = optionalChannel(
        "channel", "The channel to send the poll in", required = true
    ) { _, channel ->
        if (channel?.type != ChannelType.GuildText) {
            discordError(translate("commands.create.invalid_channel"))
        }
    }

    fun Arguments.voteTitle() = string("title", "The title of the vote")
}

fun Arguments.voteDuration() =
    optionalDuration("duration", "Amount of time after which this poll should expire.") { arg, period ->
        if (period != null && period.toDuration() < Duration.minutes(1)) {
            discordError(translate("vote.create.too_short"))
        }
    }
