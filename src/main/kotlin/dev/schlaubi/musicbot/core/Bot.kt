package dev.schlaubi.musicbot.core

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.PresenceStatus
import dev.schlaubi.mikbot.plugin.api.PluginSystem
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.util.onEach
import dev.schlaubi.musicbot.core.io.DatabaseImpl
import dev.schlaubi.musicbot.core.plugin.DefaultPluginSystem
import dev.schlaubi.musicbot.core.plugin.PluginLoader
import dev.schlaubi.musicbot.core.plugin.PluginTranslationProvider
import dev.schlaubi.musicbot.module.owner.OwnerModuleImpl
import dev.schlaubi.musicbot.module.settings.SettingsModuleImpl
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.dsl.module

class Bot : KoinComponent {

    private lateinit var bot: ExtensibleBot
    private val database: Database = DatabaseImpl()
    lateinit var translationProivder: TranslationsProvider
    internal val pluginSystem: PluginSystem = DefaultPluginSystem(this)

    suspend fun start() {
        bot = ExtensibleBot(Config.DISCORD_TOKEN) {
            PluginLoader.botPlugins.onEach {
                apply()
            }
            extensions {
                PluginLoader.botPlugins.onEach {
                    addExtensions()
                }
            }

            builtIns()
        }

        coroutineScope {
            launch {
                bot.start()
            }
            PluginLoader.botPlugins.onEach {
                atLaunch(bot)
            }
        }
    }

    private suspend fun ExtensibleBotBuilder.builtIns() {
        extensions {
            add(::SettingsModuleImpl)
            add(::OwnerModuleImpl)

            sentry {
                enable = Config.ENVIRONMENT.useSentry
                pingInReply = false

                setupCallback = {
                    setup(
                        dsn = Config.SENTRY_TOKEN,
                        beforeSend = { event, _ ->
                            event?.apply {
                                // Remove user from event
                                user = null
                            }
                        }
                    )
                }
            }
        }

        presence {
            status = PresenceStatus.DoNotDisturb
            playing("Starting ...")
        }

        chatCommands {
            enabled = false
        }

        applicationCommands {
            enabled = true

            register = true
            Config.TEST_GUILD?.let {
                defaultGuild(it)
            }
        }

        i18n {
            translationsProvider {
                translationProivder = PluginTranslationProvider {
                    defaultLocale
                }
                translationProivder
            }
        }

        hooks {
            afterKoinSetup {
                registerKoinModules()
            }
        }
    }

    private fun registerKoinModules() {
        getKoin().loadModules(
            listOf(
                module { single { database } }
            )
        )
    }
}
