package dev.schlaubi.mikbot.game.api.gdpr

import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.game.api.UserGameStats
import org.litote.kmongo.coroutine.CoroutineCollection

/**
 * Abstract implementation of [PermanentlyStoredDataPoint] for [UserGameStats].
 *
 * @property collection the [CoroutineCollection] in which the stats are saved
 */
abstract class GameStatisticsDataPoint(
    module: String,
    displayNameKey: String,
    descriptionKey: String,
    sharingDescriptionKey: String? = null
) : PermanentlyStoredDataPoint(module, displayNameKey, descriptionKey, sharingDescriptionKey) {
    abstract val collection: CoroutineCollection<UserGameStats>

    override suspend fun deleteFor(user: User) {
        collection.deleteOneById(user.id)
    }

    override suspend fun requestFor(user: User): List<String> {
        return listOf(collection.findOneById(user.id).toString())
    }
}
