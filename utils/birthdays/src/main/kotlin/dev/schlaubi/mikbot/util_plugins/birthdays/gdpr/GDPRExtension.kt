package dev.schlaubi.mikbot.util_plugins.birthdays.gdpr

import dev.kord.common.toMessageFormat
import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.util_plugins.birthdays.database.BirthdayDatabase
import org.pf4j.Extension

@Extension
class GDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(BirthDateDataPoint)
}

object BirthDateDataPoint :
    PermanentlyStoredDataPoint("birthdays", "gdpr.title", "gdpr.description", "gdpr.sharing.description") {
    override suspend fun deleteFor(user: User) {
        BirthdayDatabase.birthdays.deleteOneById(user.id)
    }

    override suspend fun requestFor(user: User): List<String> {
        val birthdate = BirthdayDatabase.birthdays.findOneById(user.id)

        return listOfNotNull(birthdate?.let { "${it.time.toMessageFormat()} - TimeZone: `${it.timeZone}`" })
    }
}
