package dev.schlaubi.uno

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
