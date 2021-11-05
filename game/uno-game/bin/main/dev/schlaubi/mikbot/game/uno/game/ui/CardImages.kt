package dev.schlaubi.mikbot.game.uno.game.ui

import dev.schlaubi.uno.cards.AbstractWildCard
import dev.schlaubi.uno.cards.Card
import dev.schlaubi.uno.cards.ColoredCard
import dev.schlaubi.uno.cards.DiscardAllCardsCard
import dev.schlaubi.uno.cards.DrawTwoCard
import dev.schlaubi.uno.cards.DrawingCard
import dev.schlaubi.uno.cards.PlayedCard
import dev.schlaubi.uno.cards.ReverseCard
import dev.schlaubi.uno.cards.SimpleCard
import dev.schlaubi.uno.cards.SkipCard
import dev.schlaubi.uno.cards.WildCard
import java.util.Locale

private const val BASE_CDN = "https://rice.by.devs-from.asia/uno/"

val Card.imageUrl: String
    get() = when (this) {
        is ColoredCard -> this.imageUrl
        is AbstractWildCard -> this.imageUrl
        else -> error("Could not find image for card: $this")
    }

val AbstractWildCard.imageUrl: String
    get() = when (this) {
        is PlayedCard -> {
            val name = if (this is DrawingCard) {
                "wilddraw4.png"
            } else {
                "wild.png"
            }
            "$coloredCdn/$name"
        }
        is WildCard -> "$BASE_CDN/wild/wild.png"
        is DrawingCard -> "$BASE_CDN/wild/draw4.png"
        else -> error("Could not find image for card: $this")
    }

val ColoredCard.imageUrl: String
    get() {
        val name = when (this) {
            is SimpleCard -> number.toString()
            is DrawTwoCard -> "draw2"
            is ReverseCard -> "reverse"
            is SkipCard -> "skip"
            is DiscardAllCardsCard -> "discard_all_cards"
        }

        return "$coloredCdn/$name.png"
    }

private val PlayedCard.coloredCdn: String
    get() = BASE_CDN + color.name.lowercase(Locale.ENGLISH)
