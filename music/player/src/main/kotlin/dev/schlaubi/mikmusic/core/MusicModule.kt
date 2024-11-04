package dev.schlaubi.mikmusic.core

import dev.kordex.core.annotations.ExtensionDSL
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.application.ApplicationCommand
import dev.kordex.core.commands.application.slash.EphemeralSlashCommand
import dev.kordex.core.commands.application.slash.EphemeralSlashCommandContext
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.event
import dev.kord.common.entity.ApplicationIntegrationType
import dev.kord.common.entity.InteractionContextType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kordex.core.types.TranslatableContext
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.getExtensions
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.module.MikBotModule
import dev.schlaubi.mikbot.plugin.api.util.*
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.checks.musicControlCheck
import dev.schlaubi.mikmusic.core.audio.LavalinkManager
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.PersistentPlayerState
import dev.schlaubi.mikmusic.player.applyToPlayer
import dev.schlaubi.mikmusic.player.voiceStateWatcher
import dev.schlaubi.mikmusic.util.TrackLinkedListSerializer
import dev.schlaubi.mikmusic.util.TrackListSerializer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import org.litote.kmongo.serialization.registerSerializer
import org.pf4j.ExtensionPoint
import kotlin.reflect.KMutableProperty1

interface MusicExtensionPoint : ExtensionPoint {
    suspend fun MusicModule.overrideSetup()
}

class MusicModule(context: PluginContext) : MikBotModule(context) {
    private val lavalink: LavalinkManager by extension()
    private val musicPlayers: MutableMap<Snowflake, MusicPlayer> = mutableMapOf()
    override val name: String = "music"
    override val allowApplicationCommandInDMs: Boolean = false

    val database: Database by inject()
    private val playerStates = database.getCollection<PersistentPlayerState>("player_states")

    val CommandContext.link: Link
        get() = lavalink.getLink(safeGuild)

    val CommandContext.player: Player
        get() = link.player

    val CommandContext.musicPlayer
        get() = getMusicPlayer(safeGuild)
    val CommandContext.node
        get() = lavalink.newNode()

    fun getMusicPlayer(guild: GuildBehavior): MusicPlayer {
        return musicPlayers.computeIfAbsent(guild.id) {
            val link = lavalink.getLink(guild)

            MusicPlayer(link, guild)
        }
    }

    internal fun unregister(guildId: Snowflake) = musicPlayers.remove(guildId)

    override suspend fun setup() {
        registerSerializer(TrackListSerializer)
        registerSerializer(TrackLinkedListSerializer)
        context.pluginSystem.getExtensions<MusicExtensionPoint>().forEach {
            with(it) {
                overrideSetup()
            }
        }

        voiceStateWatcher()
        event<ReadyEvent> {
            action {
                reconnectPlayers()
            }
        }
    }

    suspend fun EphemeralSlashCommandContext<*, *>.checkOtherSchedulerOptions(
        myProperty: KMutableProperty1<MusicPlayer, Boolean>,
        vararg properties: KMutableProperty1<MusicPlayer, Boolean>,
        callback: suspend (newValue: Boolean) -> Unit,
    ) = checkOtherSchedulerOptions(
        musicPlayer, this,
        { confirmation { it() } },
        { edit { it() } },
        myProperty,
        *properties,
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
        body: suspend T.() -> Unit,
    ) = create {
        musicControlContexts()

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
                player.applyState(it)
            }
        }
        playerStates.drop()
    }
}

fun ApplicationCommand<*>.musicControlContexts() {
    allowedInstallTypes.add(ApplicationIntegrationType.GuildInstall)
    allowedContexts.add(InteractionContextType.Guild)
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
    translator: TranslatableContext,
    confirmation: ConfirmationSender,
    edit: MessageEditor,
    myProperty: KMutableProperty1<MusicPlayer, Boolean>,
    vararg properties: KMutableProperty1<MusicPlayer, Boolean>,
    callback: suspend (newValue: Boolean) -> Unit,
) {
    if (properties.any { it.get(musicPlayer) }) {
        val (confirmed) = confirmation {
            content = translator.translate(MusicTranslations.Music.multiple_scheduler_options)
        }
        if (!confirmed) {
            edit { content = translator.translate(MusicTranslations.Music.General.aborted) }
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
