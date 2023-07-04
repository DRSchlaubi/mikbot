package dev.schlaubi.mikmusic.core.settings

import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikmusic.core.settings.commands.*
import org.pf4j.Extension

@Extension
class MusicSettingsExtension : SettingsExtensionPoint {
    override suspend fun SettingsModule.apply() {
        djModeCommand()
        fixMusicChannel()
        leaveTimeoutCommand()
        sponsorBlockCommand()
        optionsCommand()
    }
}
