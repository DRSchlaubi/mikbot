package dev.schlaubi.musicbot.core

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.cache.api.DataEntryCache
import dev.kord.common.Locale
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.event.gateway.DisconnectEvent
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.gateway.ResumedEvent
import dev.kord.rest.builder.message.allowedMentions
import dev.schlaubi.mikbot.plugin.api.MikBotInfo
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginSystem
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.getExtensions
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.util.AllShardsReadyEvent
import dev.schlaubi.musicbot.core.io.DatabaseImpl
import dev.schlaubi.musicbot.core.plugin.*
import dev.schlaubi.musicbot.core.sentry.SentryExtensionPoint
import dev.schlaubi.musicbot.module.owner.OwnerModuleImpl
import dev.schlaubi.musicbot.module.settings.SettingsModuleImpl
import dev.schlaubi.stdx.core.onEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.pf4j.PluginWrapper
import org.pf4j.update.UpdateRepository

private val LOG = KotlinLogging.logger { }

class Bot(repos: List<UpdateRepository>) : KordExKoinComponent, PluginContext {
    init {
        _pluginFactory = MikbotPluginFactory(this)
    }

    private lateinit var bot: ExtensibleBot

    override val database: Database = DatabaseImpl()
    lateinit var translationProvider: TranslationsProvider
    internal val pluginLoader = PluginLoader(repos)
    override val pluginSystem: PluginSystem = DefaultPluginSystem(this)
    override val pluginWrapper: PluginWrapper
        get() = error("Unsupported by bot context")

    suspend fun start() {
        bot = ExtensibleBot(Config.DISCORD_TOKEN) {
            kord {
                eventFlow = (pluginSystem as DefaultPluginSystem).events

                defaultDispatcher = Dispatchers.IO
                stackTraceRecovery = true

                cache {
                    messages { _, _ -> DataEntryCache.none() }
                }
            }

            pluginLoader.botPlugins.onEach {
                apply()
            }
            extensions {
                pluginLoader.botPlugins.onEach {
                    addExtensions()
                }
                add { BotModule(this@Bot) }
            }

            plugins {
                // The built-in Plugin system of KordEx is currently to feature-lacking to replace our own
                enabled = false
            }

            builtIns()
        }

        coroutineScope {
            launch {
                bot.start()
            }
            pluginLoader.botPlugins.onEach {
                atLaunch(bot)
            }
        }
    }

    private suspend fun ExtensibleBotBuilder.builtIns() {
        extensions {
            add { SettingsModuleImpl(this@Bot) }
            add { OwnerModuleImpl(this@Bot) }

            sentry {
                enable = Config.ENVIRONMENT.useSentry
                pingInReply = false

                val sentryExtensions = pluginSystem.getExtensions<SentryExtensionPoint>()
                setupCallback = {
                    init {
                        dsn = Config.SENTRY_TOKEN
                        for (extension in sentryExtensions) {
                            with(extension) {
                                setup()
                            }
                        }
                        setBeforeSend { event, hint ->
                            event.apply {
                                user = null
                                for (extension in sentryExtensions) {
                                    extension.beforeSend(this, hint)
                                }
                            }
                        }
                        setBeforeBreadcrumb { breadcrumb, hint ->
                            breadcrumb.apply {
                                for (extension in sentryExtensions) {
                                    extension.beforeBreadcrumb(breadcrumb, hint)
                                }
                            }
                        }
                    }
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
            Config.TEST_GUILD?.let(::defaultGuild)
        }

        i18n {
            interactionUserLocaleResolver()
            translationsProvider {
                translationProvider = PluginTranslationProvider(pluginLoader) {
                    defaultLocale
                }

                translationProvider
            }
            applicationCommandLocales.add(Locale.GERMAN)
        }

        hooks {
            beforeKoinSetup {
                loadModule { single { database } }
            }
        }

        // Disable all mentions in error responses
        errorResponse { message, _ ->
            allowedMentions()
            content = message
        }
    }
}

private class BotModule(private val mikbot: Bot) : Extension() {
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
                    mikbot.pluginSystem.emitEvent(AllShardsReadyEvent(kord, -1, event.customContext))
                }
                LOG.info { "Logged in with shard ${event.shard}, Remaining $remaining" }
            }
        }

        event<ResumedEvent> {
            action {
                loggedInShards += event.shard
                val indices = kord.resources.shards.indices
                val remaining = indices - loggedInShards.toSet()
                LOG.info { "Resumed with shard ${event.shard}, Remaining $remaining" }
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

        event<AllShardsReadyEvent> {
            action {
                if (mikbot.pluginLoader.plugins.none { it.pluginId == "game-animator" }) {
                    kord.editPresence {
                        playing("Mikbot v${MikBotInfo.VERSION}@${MikBotInfo.BRANCH} (${MikBotInfo.COMMIT})")
                    }
                }
            }
        }
    }
}
