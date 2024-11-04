package dev.schlaubi.mikmusic.playlist.commands

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.suggestString
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.application.slash.EphemeralSlashCommandContext
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.kordex.core.commands.converters.impl.string
import dev.schlaubi.lavakord.audio.Node
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.extension
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.playlist.Playlist
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.or
import org.litote.kmongo.util.KMongoUtil

interface PlaylistOptions {
    val name: String
}

abstract class PlaylistArguments(onlyMine: Boolean = true) : Arguments(), PlaylistOptions {
    override val name by playlistName(onlyMine)
}

fun Arguments.playlistName(onlyMine: Boolean) = string {
    name = MusicTranslations.Commands.Playlist.Arguments.Name.name
    description = MusicTranslations.Commands.Playlist.Arguments.Name.description

    validate {
        getPlaylistOrNull(context.getUser()!!, value) ?: context.notFound(value)
    }

    autoComplete {
        val genericFilter = if (onlyMine) {
            Playlist::authorId eq user.id
        } else {
            or(Playlist::public eq true, Playlist::authorId eq user.id)
        }
        val input = focusedOption.value
        val names = PlaylistDatabase.collection.find(
            and(
                genericFilter,
                KMongoUtil.toBson("{name: /$input/i}")
            )
        ).toFlow()
            .take(25)
            .toList()
        suggestString {
            names.forEach { choice(it.name, it.name) }
        }
    }
}

private suspend fun CommandContext.notFound(value: String): Nothing {
    throw DiscordRelayedException(MusicTranslations.Command.Playlist.unknown_playlist.withOrdinalPlaceholders(value))
}

private suspend fun getPlaylistOrNull(userBehavior: UserBehavior, name: String) =
    PlaylistDatabase.collection.findOne(
        and(
            Playlist::name eq name,
            or(Playlist::public eq true, Playlist::authorId eq userBehavior.id)
        )
    )

suspend fun <T> EphemeralSlashCommandContext<T, *>.getPlaylist()
    where T : Arguments, T : PlaylistOptions =
    getPlaylistOrNull(user, arguments.name) ?: error("Could not load playlist")

class PlaylistModule(context: PluginContext) : SubCommandModule(context) {
    override val name: String = "playlist"
    override val commandName = MusicTranslations.Commands.Playlist.name

    val musicModule: MusicModule by extension()
    val CommandContext.musicPlayer: MusicPlayer
        get() = with(musicModule) { musicPlayer }

    val CommandContext.node: Node
        get() = with(musicModule) { node }

    override fun SlashCommand<*, *, *>.commandSettings() {
        musicControlContexts()
    }

    override suspend fun overrideSetup() {
        loadCommand()
        saveCommand()
        renameCommand()
        removeCommand()
        deleteCommand()
        toggleVisibilityCommand()
        listCommand()
        songsCommand()
        addCommand()
    }
}

suspend inline fun EphemeralSlashCommandContext<*, *>.checkName(name: String, public: Boolean, callback: () -> Unit) {
    val nameBson = Playlist::name eq name
    val findBson = if (public) {
        and(nameBson, Playlist::public eq true)
    } else {
        and(nameBson, Playlist::authorId eq user.id)
    }

    val existingPlaylist = PlaylistDatabase.collection.findOne(findBson)
    if (existingPlaylist != null) {
        respond {
            content = translate(MusicTranslations.Commands.Playlist.Save.already_exists, name)
        }
    }

    callback()
}

suspend inline fun EphemeralSlashCommandContext<out PlaylistArguments, *>.checkPermissions(callback: (Playlist) -> Unit) {
    val playlist = getPlaylist()
    if (playlist.authorId != user.id) {
        respond {
            content = translate(MusicTranslations.Commands.Playlist.Delete.no_permission)
        }
        return
    }

    callback(playlist)
}
