package dev.schlaubi.musicbot.core.io

import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.core.audio.LavalinkServer
import dev.schlaubi.musicbot.module.music.player.PersistentPlayerState
import dev.schlaubi.musicbot.module.music.playlist.Playlist
import dev.schlaubi.musicbot.module.owner.verification.Invitation
import dev.schlaubi.musicbot.module.settings.BotGuild
import dev.schlaubi.musicbot.module.settings.BotUser
import dev.schlaubi.musicbot.utils.TrackSerializer
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.serialization.registerSerializer

class Database {
    init {
        registerSerializer(TrackSerializer)
    }

    private val client = KMongo.createClient(Config.MONGO_URL).coroutine
    private val database = client.getDatabase(Config.MONGO_DATABASE)

    val users = database.getCollection<BotUser>("users")
    val playerStates = database.getCollection<PersistentPlayerState>("player_states")
    val guildSettings = database.getCollection<BotGuild>("guild_settings")
    val playlists = database.getCollection<Playlist>("playlists")
    val lavalinkServers = database.getCollection<LavalinkServer>("lavalink_servers")
    val invitations = database.getCollection<Invitation>("invitations")
}
