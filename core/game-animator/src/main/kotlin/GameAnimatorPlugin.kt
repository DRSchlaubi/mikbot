/* ktlint-disable package-name */
package dev.schlaubi.mikbot.core.game_animator

import dev.kord.common.entity.ActivityType
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.builders.ExtensionsBuilder
import dev.kordex.core.extensions.event
import dev.schlaubi.mikbot.core.game_animator.api.GameAnimatorExtensionPoint
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.getExtensions
import dev.schlaubi.mikbot.plugin.api.module.MikBotModule
import dev.schlaubi.mikbot.plugin.api.util.AllShardsReadyEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.time.Duration.Companion.seconds

private val LOG = KotlinLogging.logger { }

@PluginMain
class GameAnimatorPlugin(wrapper: PluginContext) : Plugin(wrapper) {

    override fun ExtensionsBuilder.addExtensions() {
        add(::GameAnimator)
    }

    private class GameAnimator(context: PluginContext) : MikBotModule(context) {
        override val name: String = "GameAnimator"

        @OptIn(ObsoleteCoroutinesApi::class)
        private val ticker = ticker(30.seconds.inWholeMilliseconds, 0)
        private val extensions = context.pluginSystem.getExtensions<GameAnimatorExtensionPoint>()
        private val games: List<Game> = Config.GAMES
        private lateinit var runner: Job

        override suspend fun setup() {
            event<AllShardsReadyEvent> {
                action {
                    start()
                }
            }
        }

        private suspend fun start() {
            runner = coroutineScope {
                launch {
                    if (games.isNotEmpty()) {
                        for (unit in ticker) {
                            games.random().apply(extensions, kord)
                        }
                    } else {
                        LOG.warn { "No games set, please set the GAMES env variable" }
                    }
                }
            }
        }

        override suspend fun unload() {
            runner.cancel()
        }
    }
}

data class Game(val type: ActivityType, val status: PresenceStatus, val text: String) {
    suspend fun apply(extensions: List<GameAnimatorExtensionPoint>, kord: Kord) {
        kord.editPresence {
            status = this@Game.status

            val formattedText = extensions.fold(text) { text, extension ->
                with(extension) { text.replaceVariables() }
            }

            when (type) {
                ActivityType.Game -> playing(formattedText)
                ActivityType.Streaming -> streaming(formattedText, "https://twitch.tv/schlauhibi")
                ActivityType.Listening -> listening(formattedText)
                ActivityType.Watching -> watching(formattedText)
                ActivityType.Competing -> competing(formattedText)
                else -> error("Could not change game to $this")
            }
        }
    }

    companion object {
        fun parse(game: String): Game {
            return when (game.firstOrNull()) {
                'p' -> Game(
                    ActivityType.Game,
                    PresenceStatus.Online,
                    game.replaceFirst("p:\\s*".toRegex(), "")
                )

                'l' -> Game(
                    ActivityType.Listening,
                    PresenceStatus.Online,
                    game.replaceFirst("l:\\s*".toRegex(), "")
                )

                's' -> Game(
                    ActivityType.Streaming,
                    PresenceStatus.Online,
                    game.replaceFirst("s:\\s*".toRegex(), "")
                )

                'w' -> Game(
                    ActivityType.Watching,
                    PresenceStatus.Online,
                    game.replaceFirst("w:\\s*".toRegex(), "")
                )

                'c' -> Game(
                    ActivityType.Competing,
                    PresenceStatus.Online,
                    game.replaceFirst("c:\\s*".toRegex(), "")
                )

                else -> error("Invalid game: $this")
            }
        }
    }
}
