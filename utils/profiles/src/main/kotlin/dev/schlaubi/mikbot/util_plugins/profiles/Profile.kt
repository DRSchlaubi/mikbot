package dev.schlaubi.mikbot.util_plugins.profiles

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("_id")
    val id: Long,
    val pronouns: Set<Pronoun>,
    val badges: Set<Badge>
)
