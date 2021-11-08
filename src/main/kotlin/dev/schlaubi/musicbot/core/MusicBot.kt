package dev.schlaubi.musicbot.core

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.kord.common.entity.PresenceStatus
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.musicbot.core.plugin.PluginLoader
import dev.schlaubi.musicbot.module.owner.OwnerModuleImpl
import dev.schlaubi.musicbot.module.settings.SettingsModuleImpl
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.dsl.module

class MusicBot : KoinComponent {

    private lateinit var bot: ExtensibleBot
    private val database = Database()

    suspend fun start() {
        bot = ExtensibleBot(Config.DISCORD_TOKEN) {
            PluginLoader.botPlugins.forEach {
                with(it) {
                    apply()

                    extensions {
                        addExtensions()
                    }
                }
            }

            builtIns()
        }

        coroutineScope {
            launch {
                bot.start()
            }
            PluginLoader.botPlugins.forEach {
                with(it) {
                    atLaunch(bot)
                }
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
