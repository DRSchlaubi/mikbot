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
import dev.schlaubi.mikbot.util_plugins.profiles.Profile
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileDatabase
import dev.schlaubi.mikbot.util_plugins.profiles.Pronoun
import dev.schlaubi.mikbot.util_plugins.profiles.social.SocialAccountConnection
import dev.schlaubi.mikbot.util_plugins.profiles.social.SocialAccountConnectionType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.eq

private class ProfileArguments : Arguments() {
    val user by optionalUser(
        "user", "The user whose profile you want to see"
    )
}

private class PronounArguments : Arguments() {
    val pronoun by enumChoice<Pronoun>("Pronoun", "Choose your pronoun", "Pronoun")
}

private class ConnectArguments : Arguments() {
    val service by stringChoice(
        "social-service",
        "Choose the service to connect",
        SocialAccountConnectionType.ALL.associate { it.displayName to it.id })
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
                    embeds.add(user.renderProfile())
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
                    content = "Your profile has been updated."
                }
            }
        }

        ephemeralSubCommand(::ConnectArguments) {
            name = "connect"
            description = "Connect a social media account"
            action {
                respond {
                    embed {
                        description =
                            "Click [here](http://localhost:8080/profiles/social/connect/${arguments.service}) to connect your account."
                    }
                }
            }
        }
    }
}

private suspend fun User.renderProfile(): EmbedBuilder = coroutineScope {
    val id = id.value.toLong()
    val profile =
        async { id.findProfile() }
    val connections = ProfileDatabase.connections.find(SocialAccountConnection::userId eq id).toFlow()
        .map { it to it.type.retrieveUserFromId(it.platformId) }
        .toList()
    val pronoun = profile.await().pronouns.randomOrNull() ?: Pronoun.THEY_THEM
    embed {
        author {
            name = "$username#$discriminator"
            icon = effectiveAvatar
            url = "https://discord.com/users/$id"
        }
        description = """
            ${profile.await().badges.joinToString(separator = "\n") { "${it.emoji} | **${it.displayName}**" }}
            
            **Connected Accounts:**
            ${
            connections.joinToString(separator = "\n") { (connection, user) ->
                "**•** ${connection.type.emoji} **[${user.displayName}](${user.url})**"
            }.ifEmpty {
                "**•** :x: **No connected Accounts. :(**"
            }
        }
        
        **Pronouns:**
        ${
            profile.await().pronouns.joinToString("\n") {
                "**• [${it.displayName}](https://pronoun.is/${it.displayName})**"
            }.ifEmpty {
                "**•** :x: **No Pronouns**\nAsk them what pronouns they would like to use."
            }
        }
        
        ${pronoun.firstPerson.replaceFirstChar(Char::uppercaseChar)} created ${pronoun.thirdPerson} account on ${
            Snowflake(id).timestamp.toMessageFormat(
                DiscordTimestampStyle.LongDateTime
            )
        }
        """.trimIndent()
    }
}

private suspend inline fun Long.findProfile(): Profile {
    return ProfileDatabase.profiles.findOneById(this) ?: Profile(this, emptySet(), emptySet())
}
