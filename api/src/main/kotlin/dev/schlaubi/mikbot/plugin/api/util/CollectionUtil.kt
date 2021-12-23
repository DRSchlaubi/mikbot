package dev.schlaubi.mikbot.plugin.api.util

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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

/**
 * Performs [action] on each element of this [Iterable] in parallel.
 * Suspends until all spawned child coroutines are done.
 */
public suspend fun <T> Iterable<T>.forEachParallel(action: suspend (T) -> Unit): Unit = coroutineScope {
    forEach {
        launch {
            action(it)
        }
    }
}
