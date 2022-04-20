package dev.nycode.sponsorblock.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ActionType {
    @SerialName("skip")
    SKIP,

    @SerialName("mute")
    MUTE
}
