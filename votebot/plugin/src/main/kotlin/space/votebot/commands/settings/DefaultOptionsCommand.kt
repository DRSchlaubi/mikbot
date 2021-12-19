package space.votebot.commands.settings

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import space.votebot.command.PollSettingsArguments
import space.votebot.command.decide
import space.votebot.common.models.StoredPollSettings
import space.votebot.core.VoteBotDatabase
import space.votebot.models.UserSettings

class DefaultOptionsArgument : Arguments(), PollSettingsArguments {
    override val maxVotes by maxVotes("How many times a user is allowed to vote")
    override val maxChanges by maxChanges("How many times a user is allowed to change their vote")
    override val deleteAfterPeriod by voteDuration("Amount of time after which a poll should expire")
    override val showChartAfterClose: Boolean? by showChart("Whether to show a chart after a poll finished or not")
    override val hideResults: Boolean? by hideResults("Whether to show results only to people who voted or not")
    override val publicResults: Boolean? by publicResults("Whether to share who voted for what with the author or not")
}

suspend fun SettingsModule.defaultOptionsCommand() = ephemeralSlashCommand(::DefaultOptionsArgument) {
    name = "default-options"
    description = "Allows you to set the default options, used when creating a vote"

    action {
        val currentSettings = VoteBotDatabase.userSettings.findOneById(user.id)?.settings

        val newSettings = StoredPollSettings(
            decide(currentSettings?.deleteAfter, arguments.deleteAfter),
            decide(currentSettings?.showChartAfterClose, arguments.showChartAfterClose),
            decide(currentSettings?.maxVotes, arguments.maxVotes),
            decide(currentSettings?.maxChanges, arguments.maxChanges),
            decide(currentSettings?.hideResults, arguments.hideResults),
            decide(currentSettings?.publicResults, arguments.publicResults),
        )

        VoteBotDatabase.userSettings.save(UserSettings(user.id, newSettings))

        respond {
            content = translate("commands.default_options.saved")
        }
    }
}