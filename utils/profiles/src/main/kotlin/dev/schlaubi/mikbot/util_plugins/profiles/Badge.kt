package dev.schlaubi.mikbot.util_plugins.profiles

import kotlinx.serialization.Serializable

@Serializable
enum class Badge(val emoji: String, val displayName: String) {
    VERIFIED_MIKBOT_DEVELOPER("<:verified_bot_dev:913853771900141590>", "profiles.badges.verified_mikbot_dev"),
    CONTRIBUTOR("<:github:912756097562054666>", "profiles.badges.contributor"),
    CERTIFIED_CUTIE("<a:love_roll:777944174514274344>", "profiles.badges.certified_cutie"),
    GOOGLE_SIMP("<:google_color:652560081874845697>", "profiles.badges.google_simp")
}
