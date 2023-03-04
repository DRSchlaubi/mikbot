package dev.nycode.sponsorblock.request

import dev.nycode.sponsorblock.model.ActionType
import dev.nycode.sponsorblock.model.Category
import dev.nycode.sponsorblock.model.Service
import dev.nycode.sponsorblock.model.VideoSegment
import dev.nycode.sponsorblock.serialization.DurationSecondsSerializer
import dev.nycode.sponsorblock.serialization.VideoDurationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
public class CreateSegmentsRequestBuilder(
    @SerialName("videoID")
    public val videoId: String,
    @SerialName("userID")
    public val userId: String,
    public val userAgent: String,
    public val service: Service? = null,
    @Serializable(with = DurationSecondsSerializer::class)
    public val videoDuration: Duration? = null,
    @PublishedApi
    internal val segments: MutableList<SegmentBuilder> = mutableListOf()
) {
    public inline fun segment(segment: VideoSegment, category: Category, builder: SegmentBuilder.() -> Unit) {
        segments.add(SegmentBuilder(segment, category).apply(builder))
    }
}

@Serializable
public class SegmentBuilder(
    @Serializable(with = VideoDurationSerializer::class)
    public val segment: VideoSegment,
    public val category: Category,
    public var actionType: ActionType? = null
)
