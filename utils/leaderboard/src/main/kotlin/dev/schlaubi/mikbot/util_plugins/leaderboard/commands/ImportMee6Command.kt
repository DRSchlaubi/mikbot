package dev.schlaubi.mikbot.util_plugins.leaderboard.commands

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.util_plugins.leaderboard.core.importForGuild
import mu.KotlinLogging

private val LOG = KotlinLogging.logger { }

suspend fun SettingsModule.importMee6() = ephemeralSlashCommand {
    name = "import-mee6-leaderboard"
    description = "Tries to import the Mee6 leaderboard for this Discord"

    action {
        try {
            importForGuild(safeGuild.id)

            respond {
                content = translate("commands.import_mee6.success", "leaderboard")
            }
        } catch (e: Exception) {
            LOG.warn(e) { "An error occurred during a mee6 import" }
            respond {
                content = translate("commands.import_mee6.failed", "leaderboard")
            }
        }
    }
}
