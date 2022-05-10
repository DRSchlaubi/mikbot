package space.votebot.commands.guild

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.litote.kmongo.newId
import space.votebot.core.VoteBotDatabase
import space.votebot.core.findOneByGuild
import space.votebot.models.GuildSettings
import space.votebot.util.checkPermissions

class SetVoteChannelArguments : Arguments() {
    val channel by channel {
        name = "channel"
        description = "commands.settings.set_vote_channel.arguments.channel.description"
    }
}

suspend fun SettingsModule.addGuildSettingsCommand() = ephemeralSlashCommand {
    name = "settings"
    description = "commands.settings.description"
    guildAdminOnly()

    ephemeralSubCommand(::SetVoteChannelArguments) {
        name = "set-vote-channel"
        description = "commands.settings.set_vote_channel.description"

        action {
            val channel = arguments.channel.asChannelOfOrNull<TopGuildMessageChannel>()
                ?: discordError(translate("commands.create.invalid_channel"))
            checkPermissions(channel)

            val guildSettings =
                VoteBotDatabase.guildSettings.findOneByGuild(guild!!.id) ?: GuildSettings(newId(), guild!!.id, null)
            VoteBotDatabase.guildSettings.save(guildSettings.copy(voteChannelId = channel.id))
            respond {
                content = translate("vote.settings.vote_channel_updated")
            }
        }
    }

    ephemeralSubCommand {
        name = "remove-vote-channel"
        description = "commands.settings.remove_vote_channel.description"

        action {
            val guildSettings =
                VoteBotDatabase.guildSettings.findOneByGuild(guild!!.id) ?: GuildSettings(newId(), guild!!.id, null)
            if (guildSettings.voteChannelId == null) {
                respond {
                    content = translate("vote.settings.no_channel_defined")
                }
                return@action
            }

            VoteBotDatabase.guildSettings.save(guildSettings.copy(voteChannelId = null))
            respond {
                content = translate("vote.settings.vote_channel_removed")
            }
        }
    }
}
