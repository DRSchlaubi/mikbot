package dev.schlaubi.mikbot.util_plugins.leaderboard.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardDatabase
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardSettings

class LeaderBoardSettingsArguments : Arguments() {
    val message by optionalString {
        name = "message"
        description = "commands.leaderboard_settings.arguments.message.description"
    }

    val channel by optionalChannel {
        name = "channel"
        description = "commands.leaderboard_settings.arguments.channel.description"
        requiredChannelTypes.add(ChannelType.GuildText)
    }
}

suspend fun SettingsModule.leaderBoardCommand() =
    ephemeralSlashCommand(::LeaderBoardSettingsArguments) {
        name = "leaderboard-settings"
        description = "commands.leaderboard_settings.description"

        guildAdminOnly()

        action {
            val newSettings =
                (LeaderBoardDatabase.settings.findOneById(safeGuild.id) ?: LeaderBoardSettings(safeGuild.id))
                    .merge(arguments.channel?.id, arguments.message)

            LeaderBoardDatabase.settings.save(newSettings)

            respond {
                content = translate("commands.settings.saved.title", "leaderboard")
            }
        }
    }

private fun LeaderBoardSettings.merge(
    levelUpChannel: Snowflake?,
    levelUpMessage: String?
) = copy(
    levelUpChannel = levelUpChannel ?: this.levelUpChannel,
    levelUpMessage = levelUpMessage ?: this.levelUpMessage
)
