package space.votebot.commands.guild

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.litote.kmongo.newId
import space.votebot.core.VoteBotDatabase
import space.votebot.core.VoteBotModule
import space.votebot.core.findOneByGuild
import space.votebot.models.GuildSettings
import space.votebot.util.checkPermissions

class SetVoteChannelArguments : Arguments() {
    val channel by channel("channel", "The channel you want to set as the guild vote channel.")
}

suspend fun VoteBotModule.addGuildSettingsCommand() = ephemeralSlashCommand {
    name = "settings"
    description = "Manages settings for your guild"
    guildAdminOnly()

    ephemeralSubCommand(::SetVoteChannelArguments) {
        name = "set-vote-channel"
        description = "Sets the guild's vote channel"

        action {
            val channel = arguments.channel.asChannelOfOrNull<TopGuildMessageChannel>()
                ?: discordError(translate("vote.settings.invalid-channel-type"))
            checkPermissions(channel)

            val guildSettings =
                VoteBotDatabase.guildSettings.findOneByGuild(guild!!.id) ?: GuildSettings(newId(), guild!!.id, null)
            VoteBotDatabase.guildSettings.save(guildSettings.copy(voteChannelId = channel.id))
            respond {
                content = translate("vote.settings.vote-channel-updated")
            }
        }
    }

    ephemeralSubCommand {
        name = "remove-vote-channel"
        description = "Removes the guild's vote channel"

        action {
            val guildSettings =
                VoteBotDatabase.guildSettings.findOneByGuild(guild!!.id) ?: GuildSettings(newId(), guild!!.id, null)
            if (guildSettings.voteChannelId == null) {
                respond {
                    content = translate("vote.settings.no-channel-defined")
                }
                return@action
            }

            VoteBotDatabase.guildSettings.save(guildSettings.copy(voteChannelId = null))
            respond {
                content = translate("vote.settings.vote-channel-removed")
            }
        }
    }
}
