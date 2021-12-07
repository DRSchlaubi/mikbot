package dev.schlaubi.mikbot.plugin.api.io

import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase

/**
 * API for Database.
 *
 * @see getCollection
 */
public interface Database {
    /**
     * The [CoroutineDatabase] used for all bot collections.
     */
    public val database: CoroutineDatabase
}

/**
 * Gets a collection, with a specific default document class.
 *
 * @param name the name of the collection
 * @param <T>    the type of the class to use instead of `Document`.
 * @return the collection
 **/
public inline fun <reified T : Any> Database.getCollection(name: String): CoroutineCollection<T> =
    database.getCollection(name)
