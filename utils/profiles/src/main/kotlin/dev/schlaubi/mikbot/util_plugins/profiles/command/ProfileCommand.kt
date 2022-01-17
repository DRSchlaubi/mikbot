package dev.schlaubi.mikbot.util_plugins.profiles.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalUser
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.entity.Snowflake
import dev.kord.common.toMessageFormat
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import dev.schlaubi.mikbot.plugin.api.util.embed
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.mikbot.util_plugins.profiles.Profile
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileDatabase
import dev.schlaubi.mikbot.util_plugins.profiles.Pronoun
import dev.schlaubi.mikbot.util_plugins.profiles.social.BasicUser
import dev.schlaubi.mikbot.util_plugins.profiles.social.SocialAccountConnection
import dev.schlaubi.mikbot.util_plugins.profiles.social.serviceByName
import dev.schlaubi.mikbot.util_plugins.profiles.social.type.SocialAccountConnectionType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.and
import org.litote.kmongo.eq

private class ProfileArguments : Arguments() {
    val user by optionalUser {
        name = "user"
        description = "The user whose profile you want to see"
    }
}

private class PronounArguments : Arguments() {
    val pronoun by enumChoice<Pronoun> {
        name = "Pronoun"
        description = "Choose your pronoun"
        typeName = "Pronoun"
    }
}

private class ConnectArguments : Arguments() {
    val service by stringChoice {
        name = "social-service"
        description = "Choose the service to connect or disconnect"
        SocialAccountConnectionType.ALL.associate { it.displayName to it.id }.forEach { (name, value) ->
            choice(name, value)
        }
    }
}

suspend fun SettingsModule.profileCommand() {
    publicSlashCommand {
        name = "profile"
        description = "Use the profile system"

        publicSubCommand(::ProfileArguments) {
            name = "show"
            description = "View a users profile"

            action {
                val user = (arguments.user ?: user).asUser()
                respond {
                    embeds.add(user.renderProfile(::translate))
                }
            }
        }

        ephemeralSubCommand(::PronounArguments) {
            name = "pronoun"
            description = "Manage pronouns on your profile"
            action {
                val pronoun = arguments.pronoun
                val profile = user.id.value.toLong().findProfile()
                val newProfile = if (pronoun in profile.pronouns) {
                    profile.copy(pronouns = profile.pronouns - pronoun)
                } else {
                    profile.copy(pronouns = profile.pronouns + pronoun)
                }
                ProfileDatabase.profiles.save(newProfile)
                respond {
                    content = translate("profiles.profile_updated", "profiles")
                }
            }
        }

        ephemeralSubCommand(::ConnectArguments) {
            name = "connect"
            description = "Connect a social media account"
            action {
                respond {
                    embed {
                        description = translate(
                            "profiles.connect_service",
                            "profiles",
                            arrayOf(
                                buildBotUrl {
                                    path("profiles", "social", "connect", arguments.service)
                                }.toString()
                            )
                        )
                    }
                }
            }
        }

        publicSubCommand(::ConnectArguments) {
            name = "disconnect"
            description = "Disconnects a social service"

            action {
                val service = serviceByName(arguments.service)
                ProfileDatabase.connections.deleteMany(
                    and(
                        SocialAccountConnection::userId eq user.id.value.toLong(),
                        SocialAccountConnection::type eq service
                    )
                )

                respond {
                    content = translate("commands.profile.unlinked.success", "profiles", arrayOf(service.displayName))
                }
            }
        }
    }
}

private suspend fun User.renderProfile(translateWrapper: suspend (String, String?, Array<Any?>) -> String): EmbedBuilder =
    coroutineScope {
        suspend fun translate(
            key: String,
            bundleName: String? = "profiles",
            replacements: Array<Any?> = emptyArray()
        ) = translateWrapper(key, bundleName, replacements)

        val id = id.value.toLong()
        val profile =
            async { id.findProfile() }
        val connections = ProfileDatabase.connections.find(SocialAccountConnection::userId eq id).toFlow()
            .map {
                it to BasicUser(it.platformId, it.url, it.username)
            }.toList()

        val pronoun = profile.await().pronouns.randomOrNull() ?: Pronoun.THEY_THEM
        embed {
            author {
                name = "$username#$discriminator"
                icon = effectiveAvatar
                url = "https://discord.com/users/$id"
            }
            description = """
            ${
            profile.await().badges.map { translate(it.displayName) to it.emoji }
                .joinToString(separator = "\n") { (translation, emoji) ->
                    "$emoji | **$translation**"
                }
            }
            
            **${translate("profiles.profile.connected_accounts")}:**
            ${
            connections.joinToString(separator = "\n") { (connection, user) ->
                "**•** ${connection.type.emoji} **[${user.displayName}](${user.url})**"
            }.ifEmpty {
                "**•** :x: **${translate("profiles.profile.no_connected_accounts")}**"
            }
            }
        
        **${translate("profiles.profile.pronouns")}:**
        ${
            profile.await().pronouns.map { translate(it.displayName) to it.url }
                .joinToString("\n") { (translation, url) ->
                    "**• [$translation]($url)**"
                }.ifEmpty {
                    "**•** :x: ${translate("profiles.profile.no_pronouns")}\n${translate("profiles.profile.ask_for_pronouns")}"
                }
            }
        
        ${
            translate(
                "profiles.profile.creation_date",
                replacements = arrayOf(
                    translate(pronoun.firstPerson).replaceFirstChar(Char::uppercaseChar),
                    translate(pronoun.thirdPerson),
                    Snowflake(id).timestamp.toMessageFormat(
                        DiscordTimestampStyle.LongDateTime
                    )
                )
            )
            }
            """.trimIndent()
            thumbnail {
                url = effectiveAvatar
            }
        }
    }

private suspend inline fun Long.findProfile(): Profile {
    return ProfileDatabase.profiles.findOneById(this) ?: Profile(this, emptySet(), emptySet())
}
