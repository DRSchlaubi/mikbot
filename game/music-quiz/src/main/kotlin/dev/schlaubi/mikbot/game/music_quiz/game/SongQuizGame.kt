package dev.schlaubi.mikbot.game.music_quiz.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.Locale
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.x.emoji.Emojis
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.game.api.AutoJoinableGame
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.multiple_choice.MultipleChoiceGame
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.music_quiz.LikedSongs
import dev.schlaubi.mikbot.game.music_quiz.MusicQuizDatabase
import dev.schlaubi.mikbot.game.music_quiz.SongQuizModule
import dev.schlaubi.mikbot.game.music_quiz.toLikedSong
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.PersistentPlayerState
import dev.schlaubi.mikmusic.player.applyToPlayer
import dev.schlaubi.mikmusic.player.queue.findTrack
import dev.schlaubi.mikmusic.player.queue.spotifyUriToUrl
import dev.schlaubi.mikmusic.player.queue.toPartialSpotifyTrack
import se.michaelthelin.spotify.model_objects.specification.Track
import dev.schlaubi.lavakord.audio.player.Track as LavalinkTrack

class SongQuizGame(
    host: UserBehavior,
    module: SongQuizModule,
    quizSize: Int,
    private val musicPlayer: MusicPlayer,
    trackContainer: TrackContainer,
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider
) : MultipleChoiceGame<MultipleChoicePlayer, TrackQuestion, TrackContainer>(
    host,
    module.asType,
    quizSize,
    trackContainer
),
    AutoJoinableGame<MultipleChoicePlayer> {
    override val playerRange: IntRange = 1..10
    private var beforePlayerState: PersistentPlayerState? = null

    override suspend fun EmbedBuilder.addWelcomeMessage() {
        field {
            name = translate("game.ui.playlist")
            value = questionContainer.spotifyPlaylist.uri.spotifyUriToUrl()
        }
    }

    override fun obtainNewPlayer(user: User): MultipleChoicePlayer = SongQuizPlayer(user)
    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralMessageInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: Locale?
    ): SongQuizPlayer =
        SongQuizPlayer(user).also {
            loading.edit { content = translate(it, "song_quiz.controls.joined") }
        }

    override suspend fun askQuestion(question: TrackQuestion) {
        val lavalinkTrack = findTrack(question.track) ?: return

        musicPlayer.player.playTrack(lavalinkTrack)

        super.askQuestion(question)
    }

    override suspend fun onRejoin(event: ComponentInteractionCreateEvent, player: MultipleChoicePlayer) {
        event.interaction.respondEphemeral {
            content = translate(player, "song_quiz.controls.rejoined")
        }
    }

    override suspend fun onJoin(ack: EphemeralMessageInteractionResponseBehavior, player: MultipleChoicePlayer) {
        val member = player.user.asMember(thread.guild.id)
        val voiceState = member.getVoiceStateOrNull()
        if (voiceState?.channelId != musicPlayer.lastChannelId?.let { Snowflake(it) }) {
            ack.createEphemeralFollowup {

                content = translate(
                    player,
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

        super.runGame()
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
        super.end()
    }

    override suspend fun MessageCreateBuilder.questionUI(question: TrackQuestion) {
        actionRow {
            interactionButton(ButtonStyle.Primary, "like") {
                emoji = DiscordPartialEmoji(name = Emojis.heart.unicode)
            }
        }
    }

    override suspend fun EmbedBuilder.addQuestion(question: TrackQuestion, hideCorrectAnswer: Boolean) {
        if (hideCorrectAnswer) {
            title = question.title
        } else {
            addTrack(question.track, this@SongQuizGame)
        }
    }

    private suspend fun findTrack(track: Track): LavalinkTrack? {
        val previewLoadResult = track.previewUrl?.let { musicPlayer.loadItem(it) }

        if (previewLoadResult?.loadType == TrackResponse.LoadType.TRACK_LOADED) {
            return previewLoadResult.track.toTrack()
        }

        val youtubeTrack = track.toPartialSpotifyTrack().findTrack(musicPlayer)

        if (youtubeTrack == null) {
            thread.createMessage(translate("game.skip.song_error"))
            return null
        }
        thread.createMessage(translate("game.audio_playback.youtube"))

        return youtubeTrack
    }

    override suspend fun ComponentInteractionCreateEvent.handle(question: TrackQuestion): Boolean {
        if (interaction.componentId == "like") {
            interaction.respondEphemeral {
                val likedSongs =
                    MusicQuizDatabase.likedSongs.findOneById(interaction.user.id) ?: LikedSongs(
                        interaction.user.id,
                        emptySet()
                    )
                MusicQuizDatabase.likedSongs.save(likedSongs.copy(songs = likedSongs.songs + question.track.toLikedSong()))
                content = translate(interaction.gamePlayer!!, "song_quiz.game.liked_song")
            }
            return true
        }
        return false
    }
}

enum class GuessingMode {
    NAME,
    ARTIST
}
