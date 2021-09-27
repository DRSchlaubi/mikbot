package dev.schlaubi.musicbot.module.owner.verification

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.snowflake
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.module.owner.OwnerModule
import dev.schlaubi.musicbot.utils.confirmation
import dev.schlaubi.musicbot.utils.ownerOnly

class VerificationArguments : Arguments() {
    val guildId by snowflake("guild_id", "The guild id to toggle the verification status on")
}

suspend fun OwnerModule.unVerifyCommand() =
    ephemeralSlashCommand(::VerificationArguments) {
        name = "un-verify"
        description = "Removes the verification status from a Guild"
        ownerOnly()

        action {
            val guild = runCatching { this@ephemeralSlashCommand.kord.getGuild(arguments.guildId) }.getOrNull()

            if (guild == null) {
                respond {
                    content = translate("command.verify.unknown_id")
                }
                return@action
            }

            val botGuild = database.guildSettings.findGuild(guild)
            if (!botGuild.verified) {
                respond { content = translate("command.verify.not_verified") }
                return@action
            }

            val (confirmed) = confirmation {
                content = translate(
                    "command.verify.confirm",
                    arrayOf(guild.name)
                )
            }

            if (!confirmed) {
                respond {
                    translate("general.aborted", "general")
                }
                return@action
            }

            database.guildSettings.save(botGuild.copy(verified = false))
            guild.leave()

            respond {
                content = translate("command.verify.success", arrayOf(guild.name))
            }
        }
    }
