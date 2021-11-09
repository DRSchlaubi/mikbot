package dev.schlaubi.mikbot.plugin.api.util

import java.util.*

/**
 * Polls [amount] items from this [LinkedList].
 */
@OptIn(ExperimentalStdlibApi::class)
public fun <T> LinkedList<T>.poll(amount: Int): List<T?> = buildList(amount) {
    repeat(amount) {
        add(poll())
    }
}
