package dev.schlaubi.mikbot.core.i18n.database

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain

@PluginMain
class DatabaseI18NPlugin(wrapper: PluginContext) : Plugin(wrapper) {
    override suspend fun ExtensibleBotBuilder.apply() {
        i18n {
            localeResolver { _, _, user, _ ->
                user?.let {
                    LanguageDatabase.collection.findOneById(it.id)?.locale
                }
            }
        }
    }
}
