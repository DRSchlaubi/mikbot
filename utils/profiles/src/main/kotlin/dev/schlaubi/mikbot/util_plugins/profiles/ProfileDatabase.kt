package dev.schlaubi.mikbot.util_plugins.profiles

import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import dev.schlaubi.mikbot.util_plugins.profiles.social.SocialAccountConnection
import org.koin.core.component.KoinComponent

object ProfileDatabase : KoinComponent {
    val profiles = database.getCollection<Profile>("profiles")
    val connections = database.getCollection<SocialAccountConnection>("connections")
}
