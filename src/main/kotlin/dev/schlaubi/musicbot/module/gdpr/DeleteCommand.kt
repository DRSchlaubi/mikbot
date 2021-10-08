package dev.schlaubi.musicbot.module.gdpr

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.playlist.Playlist
import dev.schlaubi.musicbot.utils.confirmation
import dev.schlaubi.musicbot.utils.database
import org.litote.kmongo.eq

fun GDPRModule.deleteCommand() = ephemeralSubCommand {
    name = "delete"
    description = "Deletes all the persistent data the bot has on you"

    action {
        val (confirmed) = confirmation {
            content = translate("commands.gdpr.delete.confirm")
        }

        if (!confirmed) {
            return@action
        }

        database.users.deleteOneById(user.id)
        database.playlists.deleteMany(Playlist::authorId eq user.id)

        respond {
            content = translate("commands.gdpr.delete.success")
        }
    }
}
