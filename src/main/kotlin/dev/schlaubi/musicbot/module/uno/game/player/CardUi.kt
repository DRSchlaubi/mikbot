package dev.schlaubi.musicbot.module.uno.game.player

import dev.schlaubi.uno.cards.Card
import dev.schlaubi.uno.cards.PlayedCard

fun List<Card>.filterOutCards(topCard: PlayedCard) =
    distinct() // filter out duplicated cards
        .filter { it.canBePlayedOn(topCard) } // filter out cards you cannot play
