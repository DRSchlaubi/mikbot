package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.rest.builder.message.modify.actionRow
import dev.schlaubi.mikbot.game.uno.game.ui.buttonStyle
import dev.schlaubi.mikbot.game.uno.game.ui.emoji
import dev.schlaubi.mikbot.game.uno.game.ui.translationKey
import dev.schlaubi.uno.cards.Card
import dev.schlaubi.uno.cards.DrawingCard
import dev.schlaubi.uno.cards.PlayedCard

private const val cardLimit = 20

private fun Iterable<IndexedValue<Card>>.filterOutCards(topCard: PlayedCard): Pair<List<IndexedValue<Card>>, List<IndexedValue<Card>>> {
    val remainingCards = ArrayList<IndexedValue<Card>>()
    val safeCards = ArrayList<IndexedValue<Card>>()

    forEach {
        if (it !in safeCards && it.value.canBePlayedOn(topCard)) {
            safeCards.add(it)
        } else {
            remainingCards.add(it)
        }
    }

    return remainingCards.toList() to safeCards.toList()
}

suspend fun DiscordUnoPlayer.displayableCards(): List<IndexedValue<Card>> {
    val (remainingCards, safeCards) = deck
        .withIndex()
        .filterOutCards(game.game.topCard)
    return if (safeCards.size > cardLimit) {
        val diff = safeCards.size - cardLimit
        val brokenCards = safeCards.takeLast(diff)
        deck.removeAll(brokenCards.map { (_, card) -> card })
        response.createEphemeralFollowup {

            content = translate("uno.controls.removed_cards", arrayOf(diff))
        }
        safeCards.dropLast(diff)
    } else if (safeCards.size != cardLimit) { // display as many cards as possible
        safeCards + remainingCards.take(cardLimit - safeCards.size)
    } else {
        safeCards
    }
}

suspend fun DiscordUnoPlayer.pickDrawingCardToStack(): DrawingCard? {
    // It's extremely unlikely someone has more than 4 draw cards
    val cards = deck.filter { (it as? DrawingCard)?.canStackWith(game.game.topCard) == true }.take(4)
    controls.edit {
        content = translate("uno.controls.draw_card_stack", game.game.drawCardSum)

        actionRow {
            interactionButton(ButtonStyle.Danger, drawCardButton) {
                label = translate("uno.actions.draw_card")
            }

            cards.forEachIndexed { index, card ->
                interactionButton(card.buttonStyle, "play_card_$index") {
                    emoji = DiscordPartialEmoji(id = Snowflake(card.emoji), name = "1")
                    label = translate(card.translationKey)
                }
            }
        }
    }
    val response = awaitResponse { controls } ?: return null

    return if ("play_card_" in response) {
        cards[response.substringAfter("play_card_").toInt()] as DrawingCard
    } else {
        null
    }
}
