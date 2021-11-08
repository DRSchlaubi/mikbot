package dev.schlaubi.mikbot.core.i18n.database.gdpr

import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.core.i18n.database.LanguageDatabase
import org.pf4j.Extension

@Extension
class DatabaseI18NGDPR : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(languageDataPoint)
}

val languageDataPoint: PermanentlyStoredDataPoint = LanguageDataPoint

private object LanguageDataPoint : PermanentlyStoredDataPoint("database-i18n", "gdpr.name", "gdpr.description") {
    override suspend fun deleteFor(user: User) {
        LanguageDatabase.collection.deleteOneById(user.id)
    }

    override suspend fun requestFor(user: User): List<String> {
        val language = LanguageDatabase.collection.findOneById(user.id)?.locale?.let { it.getDisplayName(it) }
        return listOf(language.toString())
    }
}
