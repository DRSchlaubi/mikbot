package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.core.behavior.interaction.response.followUpEphemeral
import dev.schlaubi.uno.cards.Card
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
        response.followUpEphemeral {
            content = translate("uno.controls.removed_cards", arrayOf(diff))
        }
        safeCards.dropLast(diff)
    } else if (safeCards.size != cardLimit) { // display as many cards as possible
        safeCards + remainingCards.take(cardLimit - safeCards.size)
    } else {
        safeCards
    }
}
