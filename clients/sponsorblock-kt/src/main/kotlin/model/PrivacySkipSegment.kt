package dev.nycode.sponsorblock.model

import kotlinx.serialization.Serializable

@Serializable
public data class PrivacySkipSegment(
    val videoId: String,
    val hash: String,
    val segments: List<SkipSegment>
)
