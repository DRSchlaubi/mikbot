package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.entity.Permission
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.utils.extension
import org.koin.core.component.inject

class SettingsModule : Extension() {
    override val name: String = "settings"
    override val bundle: String = "settings"
    val database: Database by inject()
    val musicModule: MusicModule by extension()

    override suspend fun setup() {
        languageCommand()
        musicChannel()
        optionsCommand()
        fixMusicChannel()
        djModeCommand()
        sponsorBlockCommand()
    }
}

fun SlashCommand<*, *>.guildAdminOnly() {
    check {
        anyGuild()
        hasPermission(Permission.ManageGuild)
    }
}
