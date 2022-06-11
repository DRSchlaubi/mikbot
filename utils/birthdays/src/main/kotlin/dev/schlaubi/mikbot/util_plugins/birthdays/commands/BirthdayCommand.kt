package dev.schlaubi.mikbot.util_plugins.birthdays.commands

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand

suspend fun Extension.birthdayCommand() = publicSlashCommand {
    name = "birthdays"
    description = "<unused>"

    setCommand()
    listCommand()
    getCommand()
}
