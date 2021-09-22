package dev.schlaubi.musicbot.module.music

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.musicbot.core.audio.LavalinkManager
import dev.schlaubi.musicbot.module.music.commands.commands
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.utils.extension
import dev.schlaubi.musicbot.utils.safeGuild

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
}
