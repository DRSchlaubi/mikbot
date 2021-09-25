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
import dev.schlaubi.musicbot.utils.ConfirmationSender
import dev.schlaubi.musicbot.utils.MessageEditor
import dev.schlaubi.musicbot.utils.Translator
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

    suspend fun EphemeralSlashCommandContext<*>.checkOtherSchedulerOptions(
        myProperty: KMutableProperty1<MusicPlayer, Boolean>,
        vararg properties: KMutableProperty1<MusicPlayer, Boolean>,
        callback: suspend (newValue: Boolean) -> Unit
    ) = checkOtherSchedulerOptions(
        musicPlayer, ::translate,
        { confirmation { it() } },
        { edit { it() } },
        myProperty,
        *properties,
        translatorGroup = "music",
        callback = callback
    )
}

/**
 * Checks all [properties] to be false, and otherwise uses [confirmation] to confirm an overwrite to toggle [myProperty].
 *
 * @param musicPlayer the [MusicPlayer] the properties are on
 * @param translate a [Translator] to translate the messages
 * @param edit a [MessageEditor] editing the status message
 * @param callback a callback called after it got updated
 */
suspend fun checkOtherSchedulerOptions(
    musicPlayer: MusicPlayer,
    translate: Translator,
    confirmation: ConfirmationSender,
    edit: MessageEditor,
    myProperty: KMutableProperty1<MusicPlayer, Boolean>,
    vararg properties: KMutableProperty1<MusicPlayer, Boolean>,
    translatorGroup: String,
    callback: suspend (newValue: Boolean) -> Unit
) {
    if (properties.any { it.get(musicPlayer) }) {
        val (confirmed) = confirmation {
            content = translate("music.multiple_scheduler_options", translatorGroup)
        }
        if (!confirmed) {
            edit { content = translate("music.general.aborted", translatorGroup) }
            return
        } else {
            properties.forEach {
                it.set(musicPlayer, false)
            }
        }
    }

    val currentValue = myProperty.get(musicPlayer)
    myProperty.set(musicPlayer, !currentValue)

    callback(!currentValue)
}
