package dev.schlaubi.musicbot.core

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.kord.common.entity.PresenceStatus
import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.core.audio.LavalinkManager
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.core.io.findUser
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.musicchannel.MusicInteractionModule
import dev.schlaubi.musicbot.module.music.playlist.commands.PlaylistModule
import dev.schlaubi.musicbot.module.owner.OwnerModule
import dev.schlaubi.musicbot.module.settings.SettingsModule
import dev.schlaubi.musicbot.module.song_quiz.SongQuizModule
import dev.schlaubi.musicbot.module.uno.UnoModule
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
                add(::PlaylistModule)
                add(::OwnerModule)
                add(::MusicInteractionModule)
                add(::UnoModule)
                add(::SongQuizModule)
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

        coroutineScope {
            launch {
                bot.start()
            }
            launch {
                bot.findExtension<LavalinkManager>()!!.load()
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
