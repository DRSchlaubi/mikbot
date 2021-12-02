package space.votebot.common.models

import kotlinx.serialization.Serializable
import space.votebot.common.serializers.DurationSerializer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
public data class PollOptions(
    @Serializable(with = DurationSerializer::class)
    val deleteAfter: Duration
)
