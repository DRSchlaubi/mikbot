package dev.schlaubi.mikbot.util_plugins.profiles.gdpr

import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileDatabase
import dev.schlaubi.mikbot.util_plugins.profiles.social.SocialAccountConnection
import org.litote.kmongo.eq
import org.pf4j.Extension

@Extension
class ProfilesGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(
        ProfileDataPoint,
        SocialAccountExtensionPoint
    )
}

object SocialAccountExtensionPoint : PermanentlyStoredDataPoint(
    "profiles", "gdpr.connections.name", "gdpr.connections.description"
) {
    override suspend fun deleteFor(user: User) {
        ProfileDatabase.connections.deleteMany(SocialAccountConnection::userId eq user.id.value.toLong())
    }

    override suspend fun requestFor(user: User): List<String> {
        val connections = ProfileDatabase.connections
            .find(SocialAccountConnection::userId eq user.id.value.toLong())

        return connections.toList().map { "${it.type.displayName}(${it.userId}, ${it.username}, ${it.url} )" }
    }
}

object ProfileDataPoint : PermanentlyStoredDataPoint(
    "profiles", "gdpr.profile.name", "gdpr.profile.description"
) {
    override suspend fun deleteFor(user: User) {
        ProfileDatabase.profiles.deleteOneById(user.id.value.toLong())
    }

    override suspend fun requestFor(user: User): List<String> {
        val profile = ProfileDatabase.profiles.findOneById(user.id.value.toLong())
            ?: return emptyList()

        return listOf("Pronouns: ${profile.pronouns.joinToString { it.readableName }}")
    }
}
