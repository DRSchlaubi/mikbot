package dev.schlaubi.musicbot.core.io

import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.util.IKnowWhatIAmDoing
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.serialization.registerSerializer

class DatabaseImpl : Database {

    private var internalClient: CoroutineClient? = null
    private var internalDatabase: CoroutineDatabase? = null

    init {
        val url = Config.MONGO_URL
        val database = Config.MONGO_DATABASE

        if (url != null && database != null) {
            registerSerializer(DurationSerializer)
            internalClient = KMongo.createClient(url).coroutine

            internalDatabase = internalClient?.getDatabase(database)
        }
    }

    override val database: CoroutineDatabase
        get() = internalDatabase
            ?: error("Database connection is not ready on this instance, please define MONGO_URL and MONGO_DATABASE")

    @IKnowWhatIAmDoing
    override val client: CoroutineClient
        get() = internalClient
            ?: error("Database connection is not ready on this instance, please define MONGO_URL and MONGO_DATABASE")
}
