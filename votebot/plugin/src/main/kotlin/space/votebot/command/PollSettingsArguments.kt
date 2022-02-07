package space.votebot.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalEnumChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalDuration
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

    fun Arguments.emojiMode(description: String) = optionalEnumChoice<ChoiceEmojiMode> {
        name = "emoji-mode"
        this.description = description
        typeName = "EmojiMode"
    }
}

enum class ChoiceEmojiMode(override val readableName: String, val mode: PollSettings.EmojiMode) : ChoiceEnum {
    ON("Random Emojis", PollSettings.EmojiMode.ON),
    OFF("Numbers", PollSettings.EmojiMode.OFF),
    CUSTOM("Custom Emotes", PollSettings.EmojiMode.CUSTOM)
}

@Suppress("LeakingThis") // This isn't huge
abstract class AbstractPollSettingsArguments : Arguments(), PollSettingsArguments {
    override val maxVotes by maxVotes("How many times a user is allowed to vote")
    override val maxChanges by maxChanges("How many times a user is allowed to change their vote")
    override val hideResults: Boolean? by hideResults("Whether to show results only to people who voted or not")
    override val publicResults: Boolean? by publicResults("Whether to share who voted for what with the author or not")
    override val deleteAfterPeriod by voteDuration("Amount of time after which this poll should expire")
    override val showChartAfterClose: Boolean? by showChart("Whether to show a chart after the poll finished or not")
    private val emojiModeOption by emojiMode("How to use emojis in this poll")
    override val emojiMode: PollSettings.EmojiMode?
        get() = emojiModeOption?.mode
}

fun <T> decide(current: T?, new: T?): T? = new ?: current
