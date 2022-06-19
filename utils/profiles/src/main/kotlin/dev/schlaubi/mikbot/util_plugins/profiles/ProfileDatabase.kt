package dev.schlaubi.mikbot.util_plugins.profiles

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import dev.schlaubi.mikbot.util_plugins.profiles.social.SocialAccountConnection

object ProfileDatabase : KordExKoinComponent {
    val profiles = database.getCollection<Profile>("profiles")
    val connections = database.getCollection<SocialAccountConnection>("connections")
}
