package dev.schlaubi.mikbot.util_plugins.verification

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.snowflake
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.util.confirmation

class VerificationArguments : Arguments() {
    val guildId by snowflake {
        name = "guild_id"
        description = "The guild id to toggle the verification status on"
    }
}

suspend fun OwnerModule.unVerifyCommand() =
    ephemeralSlashCommand(::VerificationArguments) {
        name = "un-verify"
        description = "Removes the verification status from a Guild"

        ownerOnly()

        action {
            val guild = runCatching { this@ephemeralSlashCommand.kord.getGuildOrNull(arguments.guildId) }.getOrNull()

            if (guild == null) {
                respond {
                    content = translate("command.verify.unknown_id")
                }
                return@action
            }

            val botGuild = VerificationDatabase.collection.findOneById(guild.id)
            if (botGuild?.verified != true) {
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

            VerificationDatabase.collection.save(botGuild.copy(verified = false))
            guild.leave()

            respond {
                content = translate("command.verify.success", arrayOf(guild.name))
            }
        }
    }
