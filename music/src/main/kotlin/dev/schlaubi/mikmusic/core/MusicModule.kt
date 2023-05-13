package dev.schlaubi.mikmusic.core

import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.edit
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.module.MikBotModule
import dev.schlaubi.mikbot.plugin.api.util.*
import dev.schlaubi.mikmusic.checks.musicControlCheck
import dev.schlaubi.mikmusic.commands.commands
import dev.schlaubi.mikmusic.context.playMessageAction
import dev.schlaubi.mikmusic.core.audio.LavalinkManager
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.PersistentPlayerState
import dev.schlaubi.mikmusic.player.applyToPlayer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import kotlin.reflect.KMutableProperty1

class MusicModule(context: PluginContext) : MikBotModule(context) {
    private val lavalink: LavalinkManager by extension()
    private val musicPlayers: MutableMap<Snowflake, MusicPlayer> = mutableMapOf()
    override val name: String = "music"
    override val bundle: String = "music"
    override val allowApplicationCommandInDMs: Boolean = false

    val database: Database by inject()
    private val playerStates = database.getCollection<PersistentPlayerState>("player_states")

    val CommandContext.link: Link
        get() = lavalink.getLink(safeGuild)

    val CommandContext.player: Player
        get() = link.player

    val CommandContext.musicPlayer
        get() = getMusicPlayer(safeGuild)

    fun getMusicPlayer(guild: GuildBehavior): MusicPlayer {
        return musicPlayers.computeIfAbsent(guild.id) {
            val link = lavalink.getLink(guild)

            MusicPlayer(link, guild)
        }
    }

    override suspend fun setup() {
        commands()
        playMessageAction()

        event<ReadyEvent> {
            action {
                reconnectPlayers()
            }
        }
    }

    suspend fun EphemeralSlashCommandContext<*, *>.checkOtherSchedulerOptions(
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

    @ExtensionDSL
    suspend fun <T : Arguments> Extension.ephemeralControlSlashCommand(
        arguments: () -> T,
        body: suspend EphemeralSlashCommand<T, *>.() -> Unit,
    ): EphemeralSlashCommand<T, *> = musicApplicationCommand({ ephemeralSlashCommand(arguments, it) }, body)

    @ExtensionDSL
    suspend fun Extension.ephemeralControlSlashCommand(
        body: suspend EphemeralSlashCommand<Arguments, *>.() -> Unit,
    ): EphemeralSlashCommand<Arguments, *> = musicApplicationCommand({ ephemeralSlashCommand(it) }, body)

    private suspend fun <E : InteractionCreateEvent, T : ApplicationCommand<E>> Extension.musicApplicationCommand(
        create: suspend Extension.(suspend T.() -> Unit) -> T,
        body: suspend T.() -> Unit
    ) = create {
        check {
            musicControlCheck()
        }

        body()
    }

    suspend fun savePlayerStates() {
        val collection = playerStates
        collection.drop()
        val players = musicPlayers.filter { it.value.lastChannelId != null }.map { (_, player) -> player.toState() }
        if (players.isNotEmpty()) {
            collection.insertMany(players)
        }
    }

    suspend fun disconnect() {
        musicPlayers.forEach { (_, player) -> player.disconnectAudio() }
    }

    private suspend fun reconnectPlayers() = coroutineScope {
        val players = playerStates.find().toList()
        players.forEach {
            launch {
                val guild = kord.getGuildOrNull(it.guildId) ?: return@launch
                val channelId = it.channelId
                val player = getMusicPlayer(guild)
                it.schedulerOptions.applyToPlayer(player)
                player.connectAudio(channelId)
                it.applyToPlayer(player)
            }
        }
        playerStates.drop()
    }
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
        }
        properties.forEach {
            it.set(musicPlayer, false)
        }
    }

    val currentValue = myProperty.get(musicPlayer)
    myProperty.set(musicPlayer, !currentValue)

    callback(!currentValue)
}
