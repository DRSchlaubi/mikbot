package dev.schlaubi.musicbot.core.io

import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.io.Database
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.serialization.registerSerializer
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DatabaseImpl : Database {
    init {
        registerSerializer(DurationSerializer)
    }

    private val client = KMongo.createClient(Config.MONGO_URL).coroutine
    override val database: CoroutineDatabase = client.getDatabase(Config.MONGO_DATABASE)
}
