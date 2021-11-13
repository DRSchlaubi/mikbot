package dev.schlaubi.mikbot.util_plugins.verification

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import org.litote.kmongo.newId
import dev.schlaubi.mikbot.util_plugins.ktor.api.Config as KtorConfig

suspend fun OwnerModule.inviteCommand() = ephemeralSlashCommand(::VerificationArguments) {
    name = "Invite"
    description = "Creates a bot Invite for a specific Guild"

    ownerOnly()

    action {
        val invite = Invitation(newId(), arguments.guildId)
        VerificationDatabase.invites.save(invite)

        respond {
            content = "<" + KtorConfig.WEB_SERVER_URL + "/invitations/" + invite.id + "/accept" + ">"
        }
    }
}
