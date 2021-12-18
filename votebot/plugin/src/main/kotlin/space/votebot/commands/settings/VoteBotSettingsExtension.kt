package space.votebot.commands.settings

import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import org.pf4j.Extension

@Extension
class VoteBotSettingsExtension : SettingsExtensionPoint {
    override suspend fun SettingsModule.apply() {
        defaultOptionsCommand()
    }
}
