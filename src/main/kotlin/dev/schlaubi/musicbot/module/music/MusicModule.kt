package dev.schlaubi.musicbot.module.music

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import com.kotlindiscord.kord.extensions.interactions.edit
import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.musicbot.core.audio.LavalinkManager
import dev.schlaubi.musicbot.module.music.commands.commands
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.utils.confirmation
import dev.schlaubi.musicbot.utils.extension
import dev.schlaubi.musicbot.utils.safeGuild
import kotlin.reflect.KMutableProperty1

class MusicModule : Extension() {
    private val lavalink: LavalinkManager by extension()
    private val musicPlayers: MutableMap<Snowflake, MusicPlayer> = mutableMapOf()
    override val name: String = "music"
    override val bundle: String = "music"

    val SlashCommandContext<*, *>.link: Link
        get() = lavalink.getLink(safeGuild)

    val SlashCommandContext<*, *>.player: Player
        get() = link.player

    val SlashCommandContext<*, *>.musicPlayer
        get() = musicPlayers.computeIfAbsent(safeGuild.id) { MusicPlayer(link) }

    override suspend fun setup() {
        slashCommandCheck {
            anyGuild() // Disable this commands in DMs
        }

        commands()
    }

    suspend inline fun EphemeralSlashCommandContext<*>.checkOtherSchedulerOptions(
        myProperty: KMutableProperty1<MusicPlayer, Boolean>,
        vararg properties: KMutableProperty1<MusicPlayer, Boolean>,
        callback: (newValue: Boolean) -> Unit
    ) {
        if (properties.any { it.get(musicPlayer) }) {
            val confirmed = confirmation(this) {
                content = translate("music.multiple_scheduler_options")
            }
            if (!confirmed) {
                edit { content = translate("music.general.aborted") }
                return
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
