package dev.schlaubi.musicbot.module.uno.game.ui

import dev.kord.common.entity.ButtonStyle
import dev.schlaubi.uno.UnoColor
import dev.schlaubi.uno.cards.Card
import dev.schlaubi.uno.cards.ColoredCard

val Card.buttonStyle: ButtonStyle
    get() = when (this) {
        is ColoredCard -> buttonStyle
        else -> ButtonStyle.Secondary
    }

val UnoColor.buttonStyle: ButtonStyle
    get() = when (this) {
        UnoColor.RED -> ButtonStyle.Danger
        UnoColor.YELLOW -> ButtonStyle.Secondary
        UnoColor.BLUE -> ButtonStyle.Primary
        UnoColor.GREEN -> ButtonStyle.Success
    }

val ColoredCard.buttonStyle: ButtonStyle
    get() = color.buttonStyle

val UnoColor.localizedName: String
    get() = name
