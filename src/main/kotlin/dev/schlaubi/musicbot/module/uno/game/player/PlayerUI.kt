package dev.schlaubi.musicbot.module.uno.game.player

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.edit
import dev.kord.rest.builder.message.modify.EphemeralFollowupMessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.schlaubi.musicbot.module.uno.game.ui.buttonStyle
import dev.schlaubi.musicbot.module.uno.game.ui.emoji
import dev.schlaubi.musicbot.module.uno.game.ui.translationKey
import dev.schlaubi.uno.cards.Card

private suspend fun DiscordUnoPlayer.cardsTitle(active: Boolean, cardSize: Int): String {
    val (key, replacements) = if (active) {
        if (cardSize != deck.size) {
            "uno.controls.active.hidden.head" to arrayOf(deck.size - cardSize)
        } else {
            "uno.controls.active.head" to emptyArray()
        }
    } else {
        "uno.controls.inacvtive.head" to emptyArray()
    }

    return translate(key, *replacements)
}

suspend fun DiscordUnoPlayer.updateControls(active: Boolean) {
    controls.edit {
        val availableCards = displayableCards()
        val cards = availableCards
            .sortedBy { (card) -> card } // sort by card
            .chunked(5) // Only 5 buttons per action row

        content = cardsTitle(active, availableCards.size)

        addCards(cards, this@updateControls, active)
        addControlButtons(
            this@updateControls, active,
            availableCards.size != deck.size
        )
    }
}

private suspend fun EphemeralFollowupMessageModifyBuilder.addControlButtons(
    discordUnoPlayer: DiscordUnoPlayer,
    active: Boolean,
    cardsHidden: Boolean
) {
    actionRow {
        if (!discordUnoPlayer.drawn) {
            interactionButton(ButtonStyle.Danger, drawCardButton) {
                label = discordUnoPlayer.translate("uno.actions.draw_card")
                disabled = !active
            }
        } else {
            interactionButton(ButtonStyle.Danger, skipButton) {
                label = discordUnoPlayer.translate("uno.actions.skip")
                disabled = !active || !discordUnoPlayer.drawn
            }
        }

        if (cardsHidden) {
            interactionButton(ButtonStyle.Danger, allCardsButton) {
                label = discordUnoPlayer.translate("uno.actions.request_all_cards")
                disabled = !active
            }
        }

        if (discordUnoPlayer.deck.size <= 2) {
            interactionButton(
                if (discordUnoPlayer.saidUno) ButtonStyle.Success else ButtonStyle.Primary,
                sayUnoButton
            ) {
                label = discordUnoPlayer.translate("uno.actions.say_uno")
                disabled = !active || discordUnoPlayer.deck.size <= 1 || discordUnoPlayer.saidUno
            }
        }
    }
}

private suspend fun EphemeralFollowupMessageModifyBuilder.addCards(
    cards: List<List<Pair<Card, Int>>>,
    discordUnoPlayer: DiscordUnoPlayer,
    active: Boolean
) {
    cards.forEach {
        actionRow {
            it.forEach { (card, index) ->
                interactionButton(card.buttonStyle, "play_card_$index") {
                    emoji = DiscordPartialEmoji(id = Snowflake(card.emoji))
                    label = discordUnoPlayer.translate(card.translationKey)
                    disabled = !active || !card.canBePlayedOn(discordUnoPlayer.game.game.topCard)
                }
            }
        }
    }
}
