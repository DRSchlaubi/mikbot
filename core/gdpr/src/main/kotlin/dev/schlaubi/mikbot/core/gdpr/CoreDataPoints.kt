package dev.schlaubi.mikbot.core.gdpr

import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.AnonymizedData
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint

val SentryDataPoint = AnonymizedData(
    "gdpr",
    "gdpr.sentry.description",
    "gdpr.sentry.sharing.description"
)

object UserIdDataPoint : PermanentlyStoredDataPoint(
    "gdpr",
    "gdpr.userid.name",
    "gdpr.userid.description"
) {
    override suspend fun deleteFor(user: User) {
        // not required, as this data point itself doesn't store anything
        // and just exists for descriptive purposes
    }

    override suspend fun requestFor(user: User): List<String> = listOf(user.id.asString)
}
