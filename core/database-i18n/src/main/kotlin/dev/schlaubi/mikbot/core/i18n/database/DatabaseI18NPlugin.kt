package dev.schlaubi.mikbot.core.i18n.database

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.kord.common.Locale
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper

@PluginMain
class DatabaseI18NPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override suspend fun ExtensibleBotBuilder.apply() {
        i18n {
            defaultLocale = Config.DEFAULT_LOCALE

            applicationCommandLocales.add(Locale.GERMAN)

            localeResolver { _, _, user ->
                user?.let {
                    LanguageDatabase.collection.findOneById(it.id)?.locale
                }
            }
        }
    }
}
