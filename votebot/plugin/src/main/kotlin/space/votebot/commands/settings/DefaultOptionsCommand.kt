package space.votebot.commands.settings

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import space.votebot.command.PollSettingsArguments
import space.votebot.command.decide
import space.votebot.common.models.PollSettings
import space.votebot.common.models.StoredPollSettings
import space.votebot.core.VoteBotDatabase
import space.votebot.models.UserSettings

class DefaultOptionsArgument : Arguments(), PollSettingsArguments {
    override val maxVotes by maxVotes("commands.default_options.max_votes.description")
    override val maxChanges by maxChanges("commands.default_options.max_changes.description")
    override val deleteAfterPeriod by voteDuration("commands.default_options.delete_after_period.description")
    override val showChartAfterClose: Boolean? by showChart("commands.default_options.show_chart_after_close.description")
    override val hideResults: Boolean? by hideResults("commands.default_options.hide_results.description")
    override val publicResults: Boolean? by publicResults("commands.default_options.public_results.description")
    private val emojiModeOption by emojiMode("commands.default_options.emoji_mode.description")
    override val emojiMode: PollSettings.EmojiMode?
        get() = emojiModeOption?.mode
}

suspend fun SettingsModule.defaultOptionsCommand() = ephemeralSlashCommand(::DefaultOptionsArgument) {
    name = "default-options"
    description = "commands.default_options.description"

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
