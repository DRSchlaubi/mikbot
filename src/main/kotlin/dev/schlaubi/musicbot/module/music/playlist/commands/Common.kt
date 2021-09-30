package dev.schlaubi.musicbot.module.music.playlist.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.module.SubCommandModule
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.module.music.playlist.Playlist
import dev.schlaubi.musicbot.utils.database
import dev.schlaubi.musicbot.utils.extension
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.or

abstract class PlaylistArguments : Arguments() {
    val name by string("name", "The name of the playlist") { _, value ->
        getPlaylistOrNull(database, getUser()!!, value) ?: notFound(value)
    }

    private suspend fun CommandContext.notFound(value: String): Nothing {
        throw DiscordRelayedException(translate("command.playlist.unknown_playlist", arrayOf(value)))
    }

    suspend fun getPlaylistOrNull(database: Database, userBehavior: UserBehavior, name: String) =
        database.playlists.findOne(
            and(
                Playlist::name eq name,
                or(Playlist::public eq true, Playlist::authorId eq userBehavior.id)
            )
        )
}

suspend fun Database.updatePlaylistUsages(playlist: Playlist) {
    playlists.save(playlist.copy(usages = playlist.usages + 1))
}

suspend fun EphemeralSlashCommandContext<out PlaylistArguments>.getPlaylist() =
    arguments.getPlaylistOrNull(database, user, arguments.name) ?: error("Could not load playlist")


class PlaylistModule : SubCommandModule() {

    override val bundle: String = "music"
    override val name: String = "playlist"
    override val commandName: String = "playlist"
    override val commandDescription: String = "Manages bot playlists"

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

suspend inline fun EphemeralSlashCommandContext<*>.checkName(name: String, public: Boolean, callback: () -> Unit) {
    val nameBson = Playlist::name eq name
    val findBson = if (public) {
        and(nameBson, Playlist::public eq true)
    } else {
        and(nameBson, Playlist::authorId eq user.id)
    }

    val existingPlaylist = database.playlists.findOne(findBson)
    if (existingPlaylist != null) {
        respond {
            content = translate("commands.playlist.save.already_exists", arrayOf(name))
        }
    }

    callback()
}

suspend inline fun EphemeralSlashCommandContext<out PlaylistArguments>.checkPermissions(callback: (Playlist) -> Unit) {
    val playlist = getPlaylist()
    if (playlist.authorId != user.id) {
        respond {
            content = translate("commands.playlist.delete.no_permission")
        }
        return
    }

    callback(playlist)
}
