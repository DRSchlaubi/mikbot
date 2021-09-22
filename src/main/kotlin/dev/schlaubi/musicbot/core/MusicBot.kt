package dev.schlaubi.musicbot.core

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.kord.common.entity.PresenceStatus
import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.core.audio.LavalinkManager
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.core.io.findUser
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.settings.SettingsModule
import org.koin.core.component.KoinComponent
import org.koin.dsl.module

class MusicBot : KoinComponent {

    private lateinit var bot: ExtensibleBot
    private val database = Database()

    suspend fun start() {
        bot = ExtensibleBot(Config.DISCORD_TOKEN) {
            extensions {
                add(::GameAnimator)
                add(::SettingsModule)
                add(::LavalinkManager)
                add(::MusicModule)
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
                defaultLocale = SupportedLocales.ENGLISH
                localeResolver { _, _, user ->
                    user?.let {
                        database.users.findUser(it).language
                    }
                }
            }

            hooks {
                afterKoinSetup {
                    registerKoinModules()
                }
            }
        }

        bot.start()
    }

    private fun registerKoinModules() {
        getKoin().loadModules(
            listOf(
                module { single { database } }
            )
        )
    }
}
