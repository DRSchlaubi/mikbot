package dev.schlaubi.uno

import dev.schlaubi.uno.cards.Card
import java.util.LinkedList

/**
 * Polls [amount] items from this [LinkedList].
 */
@OptIn(ExperimentalStdlibApi::class)
public fun <T> LinkedList<T>.poll(amount: Int): List<T> = buildList(amount) {
    repeat(amount) {
        add(poll())
    }
}

@OptIn(ExperimentalStdlibApi::class)
public fun <T : Pair<Card, Int>> List<T>.dropIdentical(): List<Pair<Card, Int>> = buildList<Pair<Card, Int>> {
    val cards = mutableListOf<Card>()

    this@dropIdentical.forEach { pair ->
        if (!cards.map { it.javaClass }.contains(pair.first.javaClass)) {
            cards.add(pair.first)
            add(pair)
        }
    }
}
