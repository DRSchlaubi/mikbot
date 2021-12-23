package space.votebot.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalDuration
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.toDuration
import kotlinx.datetime.DateTimePeriod
import space.votebot.common.models.PollSettings
import kotlin.time.Duration

interface PollSettingsArguments : PollSettings {
    val deleteAfterPeriod: DateTimePeriod?
    override val deleteAfter: Duration?
        get() = deleteAfterPeriod?.toDuration()

    fun Arguments.voteDuration(description: String) =
        optionalDuration("duration", description) { _, period ->
            if (period != null && period.toDuration() < Duration.minutes(1)) {
                discordError(translate("vote.create.too_short"))
            }
        }

    fun Arguments.maxVotes(description: String) = optionalInt("max-votes", description)
    fun Arguments.maxChanges(description: String) = optionalInt("max-changes", description)
    fun Arguments.showChart(description: String) = optionalBoolean("show-chart", description)

    fun Arguments.hideResults(description: String) = optionalBoolean("hide-results", description)
    fun Arguments.publicResults(description: String) = optionalBoolean("public-results", description)
}

fun <T> decide(current: T?, new: T?): T? = new ?: current
