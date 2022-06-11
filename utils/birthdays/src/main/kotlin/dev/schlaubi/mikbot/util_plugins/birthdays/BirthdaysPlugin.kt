package dev.schlaubi.mikbot.util_plugins.birthdays

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.util_plugins.birthdays.commands.birthdayCommand

@PluginMain
class BirthdaysPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::BirthdaysModule)
    }
}

class BirthdaysModule : Extension() {
    override val name: String = "birthdays"
    override val bundle: String = "birthdays"

    override suspend fun setup() {
        birthdayCommand()
    }
}
