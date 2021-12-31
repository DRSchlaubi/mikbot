package dev.schlaubi.musicbot.core

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.event.gateway.DisconnectEvent
import dev.kord.core.event.gateway.ReadyEvent
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.pluginSystem
import dev.schlaubi.mikbot.plugin.api.util.AllShardsReadyEvent
import dev.schlaubi.mikbot.plugin.api.util.onEach
import dev.schlaubi.musicbot.core.io.DatabaseImpl
import dev.schlaubi.musicbot.core.plugin.DefaultPluginSystem
import dev.schlaubi.musicbot.core.plugin.PluginLoader
import dev.schlaubi.musicbot.core.plugin.PluginTranslationProvider
import dev.schlaubi.musicbot.module.owner.OwnerModuleImpl
import dev.schlaubi.musicbot.module.settings.SettingsModuleImpl
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.dsl.module

private val LOG = KotlinLogging.logger { }

class Bot : KoinComponent {
    private lateinit var bot: ExtensibleBot

    private val database: Database = DatabaseImpl()
    lateinit var translationProivder: TranslationsProvider
    internal val pluginSystem: DefaultPluginSystem = DefaultPluginSystem(this)

    suspend fun start() {
        bot = ExtensibleBot(Config.DISCORD_TOKEN) {
            kord {
                eventFlow = pluginSystem.events
            }

            PluginLoader.botPlugins.onEach {
                apply()
            }
            extensions {
                PluginLoader.botPlugins.onEach {
                    addExtensions()
                }
                add(::BotModule)
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

private class BotModule : Extension() {
    override val name: String = "bot"
    private var loggedIn = false

    override suspend fun setup() {
        val loggedInShards = mutableListOf<Int>()
        event<ReadyEvent> {
            action {
                loggedInShards += event.shard
                val indices = kord.resources.shards.indices
                val remaining = indices - loggedInShards.toSet()
                if (remaining.isEmpty() && !loggedIn) {
                    loggedIn = true
                    kord.editPresence {
                        status = PresenceStatus.Online
                    }
                    pluginSystem.emitEvent(AllShardsReadyEvent(kord, -1, event.coroutineContext))
                }
                LOG.info { "Logged in with shard ${event.shard}, Remaining $remaining" }
            }
        }

        event<DisconnectEvent> {
            action {
                loggedInShards -= event.shard
                LOG.warn {
                    "Shard got disconnected ${event.shard} ${event::class.simpleName}," +
                            " Awaiting login from: ${kord.resources.shards.indices - loggedInShards.toSet()}"
                }
            }
        }
    }
}
