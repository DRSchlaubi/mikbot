package dev.schlaubi.mikbot.util_plugins.profiles

import kotlinx.serialization.Serializable

@Serializable
enum class Badge(val emoji: String, val displayName: String) {
    VERIFIED_MIKBOT_DEVELOPER("<:verified_bot_dev:913853771900141590>", "profiles.badges.verified_mikbot_dev")
}
