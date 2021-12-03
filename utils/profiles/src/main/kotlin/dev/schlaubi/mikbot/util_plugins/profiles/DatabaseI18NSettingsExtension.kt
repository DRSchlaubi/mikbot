package dev.schlaubi.mikbot.util_plugins.profiles

import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.util_plugins.profiles.command.profileCommand
import org.pf4j.Extension

@Extension
class DatabaseI18NSettingsExtension : SettingsExtensionPoint {
    override suspend fun SettingsModule.apply() {
        profileCommand()
    }
}
