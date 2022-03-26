package dev.schlaubi.epic_games_notifier

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import io.ktor.http.*
import org.pf4j.Extension

@Extension
class EpicGamesNotifierSettingsExtension : SettingsExtensionPoint {
    override suspend fun SettingsModule.apply() {
        notifierCommand()
    }
}

suspend fun SettingsModule.notifierCommand() = ephemeralSlashCommand {
    name = "epic-games-notifier"
    description = "commands.epic_games_notifier.description"

    check {
        hasPermission(Permission.ManageWebhooks)
    }

    action {
        respond {
            val url = URLBuilder("https://discord.com/oauth2/authorize").apply {
                parameters.append("client_id", this@notifierCommand.kord.selfId.toString())
                parameters.append("redirect_uri", redirectUri.toString())
                parameters.append("response_type", "code")
                parameters.append("scope", "webhook.incoming")
            }.buildString()
            content = translate("commands.enable.description", "epic-games-notifier", arrayOf(url))
        }
    }
}
