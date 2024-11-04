package dev.schlaubi.mikbot.core.gdpr.api

import dev.kord.core.entity.User
import dev.kordex.core.i18n.types.Key

/**
 * Top-level representation of a type of personal data, that is being collected.
 *
 * @property module the i18n key of the module providing this data point
 * @property descriptionKey i18n key describing which data is collected and why
 * @property sharingDescriptionKey optional description key describing how and why this data is shared.
 * `null` mean this data isn't shared.
 */
sealed class DataPoint {
    abstract val descriptionKey: Key
    abstract val sharingDescriptionKey: Key?
}

/**
 * Abstract data point of data which is permanently stored within the bots own database (e.g. settings).
 *
 * @property displayNameKey i18n key for the name display in /gdpr request
 */
abstract class PermanentlyStoredDataPoint(
    val displayNameKey: Key,
    override val descriptionKey: Key,
    override val sharingDescriptionKey: Key? = null
) : DataPoint() {
    /**
     * Deletes data matching this [DataPoint] for [user].
     */
    abstract suspend fun deleteFor(user: User)

    /**
     * Requests a List of string representations of all data sets matching this [DataPoint] for [user].
     */
    abstract suspend fun requestFor(user: User): List<String>
}

/**
 * Data which is stores anonymized.
 */
class AnonymizedData(
    override val descriptionKey: Key,
    override val sharingDescriptionKey: Key?
) : DataPoint()

/**
 * Data which is only stored in memory for processing reasons and deleted immediately after it was needed.
 */
class ProcessedData(
    override val descriptionKey: Key,
    override val sharingDescriptionKey: Key?
) : DataPoint()
