package dev.schlaubi.mikmusic.core.settings

import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikbot.plugin.api.util.database
import org.koin.core.component.KoinComponent

object MusicSettingsDatabase : KoinComponent {
    val user = database.getCollection<UserSettings>("user_settings")
    val guild = database.getCollection<GuildSettings>("guild_settings")

    suspend fun findUser(user: UserBehavior) =
        this.user.findOneById(user.id) ?: UserSettings(user.id).also { this.user.save(it) }

    suspend fun findGuild(guild: GuildBehavior) =
        this.guild.findOneById(guild.id) ?: GuildSettings(guild.id).also { this.guild.save(it) }
}
