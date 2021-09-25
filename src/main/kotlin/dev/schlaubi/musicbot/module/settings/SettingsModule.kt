package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.utils.extension
import org.koin.core.component.inject

typealias SuspendFunction = suspend () -> Unit

class SettingsModule : Extension() {
    override val name: String = "settings"
    override val bundle: String = "settings"
    val database: Database by inject()
    val musicModule: MusicModule by extension()

    override suspend fun setup() {
        languageCommand()
        musicChannel()
    }
}
