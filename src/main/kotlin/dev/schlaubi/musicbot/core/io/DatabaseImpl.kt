package dev.schlaubi.musicbot.core.io

import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.io.Database
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.serialization.registerSerializer

class DatabaseImpl : Database {

    private var internalDatabase: CoroutineDatabase? = null

    init {
        val url = Config.MONGO_URL
        val database = Config.MONGO_DATABASE

        if (url != null && database != null) {
            registerSerializer(DurationSerializer)
            val client = KMongo.createClient(url).coroutine

            internalDatabase = client.getDatabase(database)
        }
    }


    override val database: CoroutineDatabase
        get() = internalDatabase
            ?: error("Database connection is not ready on this instance, please define MONGO_URL and MONGO_DATABASE")
}
