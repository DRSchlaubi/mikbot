@file:OptIn(ExperimentalTime::class)

package space.votebot.common.models

import kotlinx.serialization.Serializable
import space.votebot.common.serializers.DurationSerializer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

public interface PollSettings {
    public val deleteAfter: Duration?
    public val showChartAfterClose: Boolean?
    public val maxVotes: Int?
    public val maxChanges: Int?

    public val complete: Boolean
        get() = deleteAfter != null
                && showChartAfterClose != null
                && maxVotes != null
                && maxChanges != null

}

@Serializable
public data class StoredPollSettings(
    @Serializable(with = DurationSerializer::class) override val deleteAfter: Duration? = null,
    override val showChartAfterClose: Boolean? = null,
    override val maxVotes: Int? = null,
    override val maxChanges: Int? = null
) : PollSettings

@Serializable
public data class FinalPollSettings(
    @Serializable(with = DurationSerializer::class) override val deleteAfter: Duration?,
    override val showChartAfterClose: Boolean,
    override val maxVotes: Int,
    override val maxChanges: Int
) : PollSettings

public fun PollSettings.merge(other: PollSettings?): FinalPollSettings = FinalPollSettings(
    deleteAfter ?: other?.deleteAfter,
    showChartAfterClose ?: other?.showChartAfterClose ?: true,
    maxVotes ?: other?.maxVotes ?: 1,
    maxChanges ?: other?.maxChanges ?: 0
)
