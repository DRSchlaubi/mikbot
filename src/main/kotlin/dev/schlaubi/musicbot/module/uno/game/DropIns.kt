package dev.schlaubi.musicbot.module.uno.game

import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.disabledButton
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.types.edit
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.interaction.FollowupMessageBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.x.emoji.Emojis
import dev.schlaubi.musicbot.module.uno.game.player.DiscordUnoPlayer
import dev.schlaubi.musicbot.module.uno.game.player.translate
import dev.schlaubi.musicbot.module.uno.game.player.updateControls
import dev.schlaubi.musicbot.module.uno.game.ui.translationKey
import dev.schlaubi.uno.cards.PlayedCard
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.random.Random
import kotlin.time.Duration

private val LOG = KotlinLogging.logger { }
private val timeoutDuration = Duration.seconds(5)

suspend fun DiscordUnoGame.checkForDropIns() {
    if (allowDropIns) {
        doDropInCheck()
    }
}

private suspend fun DiscordUnoGame.doDropInCheck() {
    val availableDropIns = players
        .asSequence()
        .filter { flashMode || game.getNextPlayer() != it }
        .mapNotNull { player ->
            player.deck.firstOrNull { it is PlayedCard && it == game.topCard }
                ?.let { player to it as PlayedCard }
        }
        .toList()
    LOG.debug { "Found drop-in card: $availableDropIns" }
    if (availableDropIns.isEmpty()) return
    DropInContext(this@doDropInCheck, availableDropIns).initialize().await()
}

private class DropInContext(val game: DiscordUnoGame, val players: List<Pair<DiscordUnoPlayer, PlayedCard>>) {
    lateinit var messages: List<Pair<DiscordUnoPlayer, FollowupMessageBehavior>>
    private var completer = CompletableDeferred<Unit>()
    private lateinit var timeout: Job
    private var wonPlayer: DiscordUnoPlayer? = null

    suspend fun initialize(): CompletableDeferred<Unit> {
        messages = players.map { (player, card) ->
            player to player.dropIn(this, card)
        }

        timeout = game.launch {
            delay(timeoutDuration)
            end()
        }

        return completer
    }

    private fun end() {
        messages.forEach { (player, message) ->
            if (player.user != wonPlayer?.user) {
                game.launch {
                    message.edit {
                        components = mutableListOf()
                        content = player.translate("uno.controls.drop_in.over")
                    }
                }
            }
        }

        completer.complete(Unit)
    }

    suspend fun dropIn(player: DiscordUnoPlayer, card: PlayedCard) {
        if (wonPlayer != null) return
        wonPlayer = player
        timeout.cancel()
        game.game.dropIn(player, card)
        player.updateControls(false)
        end()
    }
}

private suspend fun DiscordUnoPlayer.dropIn(context: DropInContext, card: PlayedCard) = response.followUpEphemeral {
    content = translate("uno.controls.drop_in.title", translate(card.translationKey))

    components(timeoutDuration) {
        val correct = Random.nextInt(0, 3)
        repeat(4) {
            if (correct == it) {
                ephemeralButton {
                    style = ButtonStyle.Primary
                    id = "drop_in"
                    label = translate("uno.controls.drop_in")

                    action {
                        context.dropIn(this@dropIn, card)
                        edit {
                            components = mutableListOf()
                            content = translate("uno.controls.drop_in.success", "uno")
                        }
                    }
                }
            } else {
                disabledButton {
                    style = ButtonStyle.Secondary
                    id = "dont_drop_in_$it"
                    partialEmoji = DiscordPartialEmoji(name = Emojis.noEntrySign.unicode)
                }
            }
        }
    }
}
