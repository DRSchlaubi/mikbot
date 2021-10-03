package dev.schlaubi.musicbot.module.song_quiz.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.musicbot.game.AbstractGame
import dev.schlaubi.musicbot.game.GameStats
import dev.schlaubi.musicbot.game.module.GameModule
import dev.schlaubi.musicbot.game.translate
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.module.music.player.queue.spotifyUriToUrl
import dev.schlaubi.musicbot.module.settings.BotUser

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
    override val wonPlayers: List<SongQuizPlayer>
        get() = players.sortedByDescending { gameStats[it.user.id] ?: Statistics(0, emptyList(), quizSize) }

    override fun EmbedBuilder.addWelcomeMessage() {
        field {
            name = "Playlist"
            value = trackContainer.spotifyPlaylist.uri.spotifyUriToUrl()
        }
    }

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralInteractionResponseBehavior,
        loading: EphemeralFollowupMessage
    ): SongQuizPlayer = SongQuizPlayer(user).also {
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
        if (voiceState?.channelId?.value != musicPlayer.lastChannelId) {
            ack.followUpEphemeral {
                content =
                    translate(player.user, "song_quiz.controls.not_in_vc", "<#${musicPlayer.lastChannelId}>")
            }
        }
    }

    override suspend fun runGame() {
        musicPlayer.updateMusicChannelState(true)
        doUpdateWelcomeMessage()
        musicPlayer.clearQueue()
        val iterator = trackContainer.iterator()
        while (iterator.hasNext()) {
            turn(iterator.next(), !iterator.hasNext())
        }
    }

    override suspend fun end() {
        musicPlayer.updateMusicChannelState(false)
        musicPlayer.disconnectAudio()
        doUpdateWelcomeMessage()
        if (players.isNotEmpty() && running) {
            thread.createMessage {
                embed {
                    title = "Game final results"

                    addGameEndEmbed(this@SongQuizGame)
                }
            }
        }
    }

    override suspend fun EmbedBuilder.addWinnerGamecard() = addGameEndEmbed(this@SongQuizGame)

    override fun BotUser.applyStats(stats: GameStats): BotUser = copy(quizStats = stats)
}

enum class GuessingMode {
    NAME,
    ARTIST
}
