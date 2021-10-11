package dev.schlaubi.musicbot.module.song_quiz.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.ephemeralFollowup
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.InteractionFollowup
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.musicbot.game.AbstractGame
import dev.schlaubi.musicbot.game.GameStats
import dev.schlaubi.musicbot.game.module.GameModule
import dev.schlaubi.musicbot.game.translate
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.module.music.player.PersistentPlayerState
import dev.schlaubi.musicbot.module.music.player.applyToPlayer
import dev.schlaubi.musicbot.module.music.player.queue.spotifyUriToUrl
import dev.schlaubi.musicbot.module.settings.BotUser
import dev.schlaubi.musicbot.utils.componentLive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

private const val requestStats = "request_stats"

class SongQuizGame(
    host: UserBehavior,
    module: GameModule<SongQuizPlayer, out AbstractGame<SongQuizPlayer>>,
    val quizSize: Int,
    val musicPlayer: MusicPlayer,
    val trackContainer: TrackContainer,
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider
) : AbstractGame<SongQuizPlayer>(host, module) {
    override val playerRange: IntRange = 1..10
    val gameStats = mutableMapOf<Snowflake, Statistics>()
    private var beforePlayerState: PersistentPlayerState? = null
    override val wonPlayers: List<SongQuizPlayer>
        get() =
            players.sortedByDescending {
                gameStats[it.user.id] ?: Statistics(0, emptyList(), quizSize)
            }

    override fun EmbedBuilder.addWelcomeMessage() {
        field {
            name = "Playlist"
            value = trackContainer.spotifyPlaylist.uri.spotifyUriToUrl()
        }
    }

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralInteractionResponseBehavior,
        loading: InteractionFollowup
    ): SongQuizPlayer =
        SongQuizPlayer(user).also {
            loading.edit { content = translate(user, "song_quiz.controls.joined") }
        }

    override suspend fun onRejoin(event: ComponentInteractionCreateEvent, player: SongQuizPlayer) {
        event.interaction.respondEphemeral {
            content = translate(event.interaction.user, "song_quiz.controls.rejoined")
        }
    }

    override suspend fun onJoin(ack: EphemeralInteractionResponseBehavior, player: SongQuizPlayer) {
        val member = player.user.asMember(thread.guild.id)
        val voiceState = member.getVoiceStateOrNull()
        if (voiceState?.channelId != musicPlayer.lastChannelId?.let { Snowflake(it) }) {
            ack.ephemeralFollowup {
                content = translate(
                    player.user,
                    "song_quiz.controls.not_in_vc",
                    "<#${musicPlayer.lastChannelId}>"
                )
            }
        }
    }

    override suspend fun runGame() {
        if (musicPlayer.playingTrack != null) {
            beforePlayerState = musicPlayer.toState()
        }

        musicPlayer.updateMusicChannelState(true)
        doUpdateWelcomeMessage()
        musicPlayer.clearQueue()
        val iterator = trackContainer.iterator()
        while (iterator.hasNext()) {
            turn(iterator.next())
        }
    }

    @OptIn(KordPreview::class)
    override suspend fun end() {
        musicPlayer.updateMusicChannelState(false)
        if (!running) return
        val state = beforePlayerState
        if (state == null) {
            musicPlayer.disconnectAudio()
        } else {
            // restore player state from before the quiz
            state.schedulerOptions.applyToPlayer(musicPlayer)
            state.applyToPlayer(musicPlayer)
        }
        doUpdateWelcomeMessage()
        launch {
            endStats()
        }
    }

    @OptIn(KordPreview::class)
    private suspend fun endStats() {
        if (players.isNotEmpty() && running) {
            val message = thread.createMessage {
                embed {
                    title = "Game final results"

                    addGameEndEmbed(this@SongQuizGame)
                }

                actionRow {
                    interactionButton(ButtonStyle.Primary, requestStats) {
                        label = "See how bad you were"
                    }
                }
            }
            val live = message.componentLive(message.kord)
            live.onInteraction {
                val user = interaction.user
                val winner = wonPlayers.firstOrNull()?.user
                val statistics = gameStats[interaction.user.id]
                interaction.respondEphemeral {
                    if (statistics == null) {
                        content = translate(user, "song_quiz.game.not_in_game")
                    } else if (user.id == winner?.id) {
                        content = translate(user, "song_quiz.game.won")
                    } else {
                        embed {
                            addUserStats(
                                user,
                                gameStats[user.id] ?: Statistics(
                                    0,
                                    emptyList(), quizSize
                                )
                            )
                        }
                    }
                }
            }

            delay(Duration.minutes(1))
            message.edit { components = mutableListOf() }
            live.shutDown()
        }
    }

    override suspend fun EmbedBuilder.addWinnerGamecard() = addGameEndEmbed(this@SongQuizGame)

    override fun BotUser.applyStats(stats: GameStats): BotUser = copy(quizStats = stats)
}

enum class GuessingMode {
    NAME,
    ARTIST
}
