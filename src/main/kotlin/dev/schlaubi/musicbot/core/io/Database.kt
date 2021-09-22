package dev.schlaubi.musicbot.core.io

import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.core.audio.LavalinkServer
import dev.schlaubi.musicbot.module.settings.BotUser
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class Database {
    private val client = KMongo.createClient(Config.MONGO_URL).coroutine
    private val database = client.getDatabase(Config.MONGO_DATABASE)

    val users = database.getCollection<BotUser>("users")
    val lavalinkServers = database.getCollection<LavalinkServer>("lavalink_servers")
}
