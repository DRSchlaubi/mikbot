package dev.schlaubi.mikbot.core.gdpr

import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.AnonymizedData
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.translations.GdprTranslations

val SentryDataPoint = AnonymizedData(
    GdprTranslations.Gdpr.Sentry.description,
    GdprTranslations.Gdpr.Sentry.Sharing.description,
)

object UserIdDataPoint : PermanentlyStoredDataPoint(
    GdprTranslations.Gdpr.Userid.name,
    GdprTranslations.Gdpr.Userid.description,
) {
    override suspend fun deleteFor(user: User) {
        // not required, as this data point itself doesn't store anything
        // and just exists for descriptive purposes
    }

    override suspend fun requestFor(user: User): List<String> = listOf(user.id.toString())
}
