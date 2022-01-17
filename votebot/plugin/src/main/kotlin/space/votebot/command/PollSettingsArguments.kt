package space.votebot.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalDuration
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalEnum
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.toDuration
import kotlinx.datetime.DateTimePeriod
import space.votebot.common.models.PollSettings
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface PollSettingsArguments : PollSettings {
    val deleteAfterPeriod: DateTimePeriod?
    override val deleteAfter: Duration?
        get() = deleteAfterPeriod?.toDuration()

    fun Arguments.voteDuration(description: String) = optionalDuration {
        name = "duration"
        this.description = description

        validate {
            if (value != null && value!!.toDuration() < 1.minutes) {
                discordError(translate("vote.create.too_short"))
            }
        }
    }

    fun Arguments.maxVotes(description: String) = optionalInt {
        name = "max-votes"
        this.description = description
    }

    fun Arguments.maxChanges(description: String) = optionalInt {
        name = "max-changes"
        this.description = description
    }

    fun Arguments.showChart(description: String) = optionalBoolean {
        name = "show-chart"
        this.description = description
    }

    fun Arguments.hideResults(description: String) = optionalBoolean {
        name = "hide-results"
        this.description = description
    }

    fun Arguments.publicResults(description: String) = optionalBoolean {
        name = "public-results"
        this.description = description
    }

    fun Arguments.emojiMode(description: String) = optionalEnum<PollSettings.EmojiMode> {
        name = "emoji-mode"
        this.description = description
        typeName = "EmojiMode"
    }
}
@Suppress("LeakingThis") // This isn't huge
abstract class AbstractPollSettingsArguments : Arguments(), PollSettingsArguments {
    override val maxVotes by maxVotes("How many times a user is allowed to vote")
    override val maxChanges by maxChanges("How many times a user is allowed to change their vote")
    override val hideResults: Boolean? by hideResults("Whether to show results only to people who voted or not")
    override val publicResults: Boolean? by publicResults("Whether to share who voted for what with the author or not")
    override val deleteAfterPeriod by voteDuration("Amount of time after which this poll should expire")
    override val showChartAfterClose: Boolean? by showChart("Whether to show a chart after the poll finished or not")
    override val emojiMode: PollSettings.EmojiMode? by emojiMode("How to use emojis in this poll")
}

fun <T> decide(current: T?, new: T?): T? = new ?: current
