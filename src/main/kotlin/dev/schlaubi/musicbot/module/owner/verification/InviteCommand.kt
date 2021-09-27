package dev.schlaubi.musicbot.module.owner.verification

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.module.owner.OwnerModule
import dev.schlaubi.musicbot.utils.database
import dev.schlaubi.musicbot.utils.ownerOnly
import org.litote.kmongo.newId

suspend fun OwnerModule.inviteCommand() = ephemeralSlashCommand(::VerificationArguments) {
    name = "Invite"
    description = "Creates a bot Invite for a specific Guild"
    ownerOnly()

    action {
        val invite = Invitation(newId(), arguments.guildId)
        database.invitations.save(invite)

        respond {
            content = "<" + Config.VERIFY_SERVER_URL + "/invitations/" + invite.id + "/accept" + ">"
        }
    }
}
