package dev.nycode.sponsorblock.model

import dev.nycode.sponsorblock.serialization.DurationMinutesSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
public data class UserInfo(
    @SerialName("userID")
    val userId: String,
    @SerialName("userName")
    val username: String,
    @Serializable(with = DurationMinutesSerializer::class)
    val savedTime: Duration,
    val segmentCount: Int,
    val ignoredSegmentCount: Int,
    val viewCount: Int,
    val ignoredViewCount: Int,
    val warnings: Int,
    val warningReason: String,
    val reputation: Double,
    @SerialName("vip")
    val isVip: Boolean,
    val lastSegmentId: String?
)
