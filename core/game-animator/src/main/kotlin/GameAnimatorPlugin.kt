package dev.schlaubi.mikbot.core.game_animator

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.ActivityType
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.schlaubi.mikbot.core.game_animator.api.GameAnimatorExtensionPoint
import dev.schlaubi.mikbot.plugin.api.*
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
class GameAnimatorPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::GameAnimator)
    }

    private class GameAnimator : Extension() {
        override val name: String = "GameAnimator"

        @OptIn(ObsoleteCoroutinesApi::class)
        private val ticker = ticker(30.seconds.inWholeMilliseconds, 0)
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
                            games.random().apply(kord)
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
    val extensions = pluginSystem.getExtensions<GameAnimatorExtensionPoint>()

    suspend fun apply(kord: Kord) {
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
                    game.replaceFirst("p: ".toRegex(), "")
                )

                'l' -> Game(
                    ActivityType.Listening,
                    PresenceStatus.Online,
                    game.replaceFirst("l: ".toRegex(), "")
                )

                's' -> Game(
                    ActivityType.Streaming,
                    PresenceStatus.Online,
                    game.replaceFirst("s: ".toRegex(), "")
                )

                'w' -> Game(
                    ActivityType.Watching,
                    PresenceStatus.Online,
                    game.replaceFirst("w: ".toRegex(), "")
                )

                'c' -> Game(
                    ActivityType.Competing,
                    PresenceStatus.Online,
                    game.replaceFirst("c: ".toRegex(), "")
                )

                else -> error("Invalid game: $this")
            }
        }
    }
}
