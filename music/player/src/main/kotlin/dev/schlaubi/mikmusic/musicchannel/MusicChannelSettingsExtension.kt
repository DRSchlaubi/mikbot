package dev.schlaubi.mikmusic.musicchannel

import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import org.pf4j.Extension

@Extension
class MusicChannelSettingsExtension : SettingsExtensionPoint {
    override suspend fun SettingsModule.apply() {
        musicChannel()
    }
}
