package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.extension
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.playlist.Playlist
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.or

abstract class PlaylistArguments : Arguments() {
    val name by string {
        name = "name"
        description = "The name of the playlist"

        validate {
            getPlaylistOrNull(context.getUser()!!, value) ?: context.notFound(value)
        }
    }

    private suspend fun CommandContext.notFound(value: String): Nothing {
        throw DiscordRelayedException(translate("command.playlist.unknown_playlist", arrayOf(value)))
    }

    suspend fun getPlaylistOrNull(userBehavior: UserBehavior, name: String) =
        PlaylistDatabase.collection.findOne(
            and(
                Playlist::name eq name,
                or(Playlist::public eq true, Playlist::authorId eq userBehavior.id)
            )
        )
}

suspend fun EphemeralSlashCommandContext<out PlaylistArguments, *>.getPlaylist() =
    arguments.getPlaylistOrNull(user, arguments.name) ?: error("Could not load playlist")

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
