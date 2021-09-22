package dev.schlaubi.musicbot.core

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.ActivityType
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.schlaubi.musicbot.config.Config
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class GameAnimator : Extension() {
    override val name: String = "GameAnimator"

    @OptIn(ExperimentalTime::class, ObsoleteCoroutinesApi::class)
    private val ticker = ticker(Duration.seconds(30).inWholeMilliseconds, 0)
    private val games: List<Game> = Config.GAMES.map { Game.parse(it) }
    private lateinit var runner: Job

    override suspend fun setup() {
        event<ReadyEvent> {
            action { start() }
        }
    }

    private suspend fun start() {
        runner = coroutineScope {
            launch {
                for (unit in ticker) {
                    games.random().apply(kord)
                }
            }
        }
    }

    override suspend fun unload() {
        runner.cancel()
    }

    @JvmRecord
    data class Game(val type: ActivityType, val status: PresenceStatus, val text: String) {
        suspend fun apply(kord: Kord) {
            kord.editPresence {
                status = this@Game.status

                when (type) {
                    ActivityType.Game -> playing(text)
                    ActivityType.Streaming -> streaming(text, "https://twitch.tv/schlauhibi")
                    ActivityType.Listening -> listening(text)
                    ActivityType.Watching -> watching(text)
                    ActivityType.Competing -> competing(text)
                    else -> error("Could not change game to $this")
                }
            }
        }

        companion object {
            fun parse(game: String): Game {
                if (game.startsWith("p: ")) return Game(
                    ActivityType.Game,
                    PresenceStatus.Online,
                    game.replaceFirst(
                        "p: ".toRegex(),
                        ""
                    )
                ) else if (game.startsWith("l: ")) return Game(
                    ActivityType.Listening,
                    PresenceStatus.Online,
                    game.replaceFirst(
                        "l: ".toRegex(),
                        ""
                    )
                ) else if (game.startsWith("s: ")) return Game(
                    ActivityType.Streaming,
                    PresenceStatus.Online,
                    game.replaceFirst("s: ".toRegex(), ""),
                ) else if (game.startsWith("w: ")) return Game(
                    ActivityType.Watching,
                    PresenceStatus.Online,
                    game.replaceFirst(
                        "w: ".toRegex(),
                        ""
                    )
                ) else {
                    error("Invalid game: $this")
                }
            }
        }
    }
}
