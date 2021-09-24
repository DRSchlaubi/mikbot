package dev.schlaubi.musicbot.module.music

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import com.kotlindiscord.kord.extensions.interactions.edit
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.musicbot.core.audio.LavalinkManager
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.module.music.checks.musicControlCheck
import dev.schlaubi.musicbot.module.music.commands.commands
import dev.schlaubi.musicbot.module.music.context.playMessageAction
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.utils.confirmation
import dev.schlaubi.musicbot.utils.extension
import dev.schlaubi.musicbot.utils.safeGuild
import org.koin.core.component.inject
import kotlin.reflect.KMutableProperty1

class MusicModule : Extension() {
    private val lavalink: LavalinkManager by extension()
    private val musicPlayers: MutableMap<Snowflake, MusicPlayer> = mutableMapOf()
    override val name: String = "music"
    override val bundle: String = "music"

    val database: Database by inject()

    val CommandContext.link: Link
        get() = lavalink.getLink(safeGuild)

    val CommandContext.player: Player
        get() = link.player

    val CommandContext.musicPlayer
        get() = getMusicPlayer(safeGuild)

    fun getMusicPlayer(guild: GuildBehavior) =
        musicPlayers.computeIfAbsent(guild.id) {
            val link = lavalink.getLink(guild)

            MusicPlayer(link, guild, database)
        }

    override suspend fun setup() {
        slashCommandCheck {
            anyGuild() // Disable this commands in DMs
            musicControlCheck() // checks voice connection etc.
        }

        commands()
        playMessageAction()
    }

    suspend inline fun EphemeralSlashCommandContext<*>.checkOtherSchedulerOptions(
        myProperty: KMutableProperty1<MusicPlayer, Boolean>,
        vararg properties: KMutableProperty1<MusicPlayer, Boolean>,
        callback: (newValue: Boolean) -> Unit
    ) {
        if (properties.any { it.get(musicPlayer) }) {
            val confirmation = confirmation {
                content = translate("music.multiple_scheduler_options")
            }
            if (!confirmation.value) {
                edit { content = translate("music.general.aborted") }
                return
            } else {
                confirmation.value
            }

            properties.forEach {
                it.set(musicPlayer, false)
            }
        }

        val currentValue = myProperty.get(musicPlayer)
        myProperty.set(musicPlayer, !currentValue)

        callback(!currentValue)
    }
}
