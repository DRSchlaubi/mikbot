package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.schlaubi.musicbot.core.io.Database
import org.koin.core.component.inject

class SettingsModule : Extension() {
    override val name: String = "settings"
    override val bundle: String = "settings"
    val database: Database by inject()

    override suspend fun setup() {
        languageCommand()
    }
}
