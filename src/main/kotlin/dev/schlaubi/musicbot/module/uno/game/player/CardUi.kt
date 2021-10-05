package dev.schlaubi.musicbot.module.uno.game.player

import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.schlaubi.uno.cards.Card
import dev.schlaubi.uno.cards.PlayedCard

private const val cardLimit = 20

private fun List<Card>.filterOutCards(topCard: PlayedCard): Pair<List<Card>, List<Card>> {
    val remainingCards = ArrayList<Card>(size)
    val safeCards = ArrayList<Card>(size)

    forEach {
        if (it !in safeCards && it.canBePlayedOn(topCard)) {
            safeCards.add(it)
        } else {
            remainingCards.add(it)
        }
    }

    return remainingCards.toList() to safeCards.toList()
}

suspend fun DiscordUnoPlayer.playableCards(): List<Card> {
    val (remainingCards, safeCards) = deck.filterOutCards(game.game.topCard)
    return if (safeCards.size > cardLimit) {
        val diff = safeCards.size - cardLimit
        val brokenCards = safeCards.takeLast(diff)
        deck.removeAll(brokenCards)
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
