package dev.schlaubi.mikbot.plugin.api.io

import dev.schlaubi.mikbot.plugin.api.config.Config
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.serialization.registerSerializer

public class Database {
    init {
        registerSerializer(DurationSerializer)
    }

    private val client = KMongo.createClient(Config.MONGO_URL).coroutine

    @PublishedApi
    internal val database: CoroutineDatabase = client.getDatabase(Config.MONGO_DATABASE)

    public inline fun <reified T : Any> getCollection(name: String): CoroutineCollection<T> =
        database.getCollection(name)

//    val users = database.getCollection<BotUser>("users")
//    val guildSettings = database.getCollection<BotGuild>("guild_settings")
//    val playlists = database.getCollection<Playlist>("playlists")
//    val invitations = database.getCollection<Invitation>("invitations")
}
