package dev.nycode.sponsorblock.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class Category(private val stringValue: String) {
    @SerialName("sponsor")
    SPONSOR("sponsor"),

    @SerialName("selfpromo")
    SELF_PROMO("selfpromo"),

    @SerialName("interaction")
    INTERACTION("interaction"),

    @SerialName("intro")
    INTRO("intro"),

    @SerialName("outro")
    OUTRO("outro"),

    @SerialName("preview")
    PREVIEW("preview"),

    @SerialName("music_offtopic")
    MUSIC_OFF_TOPIC("music_offtopic");

    override fun toString(): String {
        return stringValue
    }
}
