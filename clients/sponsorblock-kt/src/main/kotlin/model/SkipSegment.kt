package dev.nycode.sponsorblock.model

import dev.nycode.sponsorblock.serialization.DurationSecondsSerializer
import dev.nycode.sponsorblock.serialization.VideoDurationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

public typealias VideoSegment = Pair<Float, Float>

@Serializable
public data class SkipSegment(
    @Serializable(with = VideoDurationSerializer::class)
    val segment: VideoSegment,
    @SerialName("UUID")
    val uuid: String,
    val category: Category,
    @Serializable(with = DurationSecondsSerializer::class)
    val videoDuration: Duration,
    val actionType: String
)
