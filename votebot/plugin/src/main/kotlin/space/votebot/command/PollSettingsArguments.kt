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

    fun Arguments.voteDuration(description: String) = optionalDuration(
        "duration",
        description, required = true
    ) { _, period ->
        if (period != null && period.toDuration() < 1.minutes) {
            discordError(translate("vote.create.too_short"))
        }
    }

    fun Arguments.maxVotes(description: String) = optionalInt("max-votes", description, required = true)
    fun Arguments.maxChanges(description: String) = optionalInt("max-changes", description, required = true)
    fun Arguments.showChart(description: String) = optionalBoolean("show-chart", description, required = true)

    fun Arguments.hideResults(description: String) = optionalBoolean("hide-results", description, required = true)
    fun Arguments.publicResults(description: String) = optionalBoolean("public-results", description, required = true)
    fun Arguments.emojiMode(description: String) = optionalEnum<PollSettings.EmojiMode>(
        "emoji-mode", description, required = true, "EmojiMode"
    )
}

fun <T> decide(current: T?, new: T?): T? = new ?: current
