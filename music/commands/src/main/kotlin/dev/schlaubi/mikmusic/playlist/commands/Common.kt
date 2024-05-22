package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.suggestString
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.extension
import dev.schlaubi.mikmusic.core.MusicModule
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
    name = "name"
    description = "The name of the playlist"

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
    throw DiscordRelayedException(translate("command.playlist.unknown_playlist", arrayOf(value)))
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

    override val bundle: String = "music"
    override val name: String = "playlist"
    override val commandName: String = "playlist"

    val musicModule: MusicModule by extension()
    val CommandContext.musicPlayer: MusicPlayer
        get() = with(musicModule) { musicPlayer }

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
            content = translate("commands.playlist.save.already_exists", arrayOf(name))
        }
    }

    callback()
}

suspend inline fun EphemeralSlashCommandContext<out PlaylistArguments, *>.checkPermissions(callback: (Playlist) -> Unit) {
    val playlist = getPlaylist()
    if (playlist.authorId != user.id) {
        respond {
            content = translate("commands.playlist.delete.no_permission")
        }
        return
    }

    callback(playlist)
}
