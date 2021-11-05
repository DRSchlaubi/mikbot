package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.schlaubi.uno.cards.Card
import dev.schlaubi.uno.cards.PlayedCard

private const val cardLimit = 20

typealias CardIntPair = Pair<Card, Int>

private fun List<CardIntPair>.filterOutCards(topCard: PlayedCard): Pair<List<CardIntPair>, List<CardIntPair>> {
    val remainingCards = ArrayList<CardIntPair>(size)
    val safeCards = ArrayList<CardIntPair>(size)

    forEach {
        if (it !in safeCards && it.first.canBePlayedOn(topCard)) {
            safeCards.add(it)
        } else {
            remainingCards.add(it)
        }
    }

    return remainingCards.toList() to safeCards.toList()
}

suspend fun DiscordUnoPlayer.displayableCards(): List<Pair<Card, Int>> {
    val (remainingCards, safeCards) = deck
        .mapIndexed { index, card -> card to index }
        .filterOutCards(game.game.topCard)
    return if (safeCards.size > cardLimit) {
        val diff = safeCards.size - cardLimit
        val brokenCards = safeCards.takeLast(diff)
        deck.removeAll(brokenCards.map { (card) -> card })
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
