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

/**
 * Performs the given [action] on each element.
 */
public inline fun <T> Iterable<T>.onEach(action: T.() -> Unit): Unit = forEach { it.action() }
