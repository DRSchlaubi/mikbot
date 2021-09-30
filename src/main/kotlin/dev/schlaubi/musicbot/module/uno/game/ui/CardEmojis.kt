package dev.schlaubi.musicbot.module.uno.game.ui

import dev.schlaubi.uno.UnoColor
import dev.schlaubi.uno.cards.AbstractWildCard
import dev.schlaubi.uno.cards.Card
import dev.schlaubi.uno.cards.ColoredCard
import dev.schlaubi.uno.cards.DrawTwoCard
import dev.schlaubi.uno.cards.DrawingCard
import dev.schlaubi.uno.cards.PlayedCard
import dev.schlaubi.uno.cards.ReverseCard
import dev.schlaubi.uno.cards.SimpleCard
import dev.schlaubi.uno.cards.SkipCard

val Card.emoji: Long
    get() = when (this) {
        is ColoredCard -> this.emoji
        is AbstractWildCard -> this.emoji
        else -> error("Could not find image for card: $this")
    }

val AbstractWildCard.emoji: Long
    get() = when (this) {
        is PlayedCard -> {
            when (color) {
                UnoColor.RED -> s(893104210982359070, 893104211292741632)
                UnoColor.YELLOW -> s(893104211036889138, 893104210864926721)
                UnoColor.BLUE -> s(893104210936209408, 893104210604883990)
                UnoColor.GREEN -> s(893104210877505558, 893104210869092463)
            }
        }
        else -> s(893103425842200596, 893103425947050044)
    }

val ColoredCard.emoji: Long
    get() = when (color) {
        UnoColor.RED -> redColor
        UnoColor.YELLOW -> yellowColor
        UnoColor.BLUE -> blueColor
        UnoColor.GREEN -> greenColor
    }

private val ColoredCard.redColor
    get() = s(
        893105958593634375,
        893105958853677076,
        893105958845284373,
        893105958857887754,
        893105958845284372,
        893105959239548968,
        893105958979534878,
        893105958883041320,
        893105958878867496,
        893105958753038376,
        893105958606217257,
        893105958694289448,
        893105958761410561
    )

private val ColoredCard.yellowColor
    get() = s(
        893106808619667477,
        893106808229621771,
        893106808619667476,
        893106808644829215,
        893106808640647279,
        893106808577732621,
        893106808657432587,
        893106808556757002,
        893106808577732618,
        893106808544186438,
        893106808439341076,
        893106808443531324,
        893106808250564659
    )

private val ColoredCard.blueColor
    get() = s(
        893108289284505630,
        893108289309646858,
        893108289284505631,
        893108289146069064,
        893108289284472852,
        893108289276084284,
        893108289326428160,
        893108289129295873,
        893108289343217684,
        893108289339019264,
        893108289536147516,
        893108289531936809,
        893108289393532938
    )

private val ColoredCard.greenColor
    get() = s(
        893107335726260264,
        893107335449415690,
        893107335613022268,
        893107335415889980,
        893107335524933692,
        893107335290048522,
        893107335294238761,
        893107335373942884,
        893107335294238760,
        893107335441051659,
        893107335424245771,
        893107335424245770,
        893107335327809557
    )

private fun AbstractWildCard.s(normal: Long, drawing: Long): Long = if (this is DrawingCard) drawing else normal

private fun ColoredCard.s(
    e0: Long,
    e1: Long,
    e2: Long,
    e3: Long,
    e4: Long,
    e5: Long,
    e6: Long,
    e7: Long,
    e8: Long,
    e9: Long,
    skip: Long,
    reverse: Long,
    draw2: Long
): Long {
    return when (this) {
        is ReverseCard -> reverse
        is SkipCard -> skip
        is DrawTwoCard -> draw2
        else -> when (val number = (this as SimpleCard).number) {
            0 -> e0
            1 -> e1
            2 -> e2
            3 -> e3
            4 -> e4
            5 -> e5
            6 -> e6
            7 -> e7
            8 -> e8
            9 -> e9
            else -> error("Invalid number: $number ($this)")
        }
    }
}
