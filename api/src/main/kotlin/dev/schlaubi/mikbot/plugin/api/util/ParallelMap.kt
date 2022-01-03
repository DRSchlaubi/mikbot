package dev.schlaubi.mikbot.plugin.api.util

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Maps every element of this collection using [mapper] in parallel.
 *
 * @param maxConcurrentRequests the maximum amount of concurrent coroutines (`null` means unlimited)
 */
public suspend fun <T, S> Collection<T>.parallelMap(
    maxConcurrentRequests: Int? = null,
    mapper: suspend (T) -> S
): List<S> = parallelMapIndexed(maxConcurrentRequests) { _, t -> mapper(t) }

/**
 * Maps every element of this collection using [mapper] in parallel and filters out `null`.
 *
 * @param maxConcurrentRequests the maximum amount of concurrent coroutines (`null` means unlimited)
 */
public suspend fun <T, S> Collection<T>.parallelMapNotNull(
    maxConcurrentRequests: Int? = null,
    mapper: suspend (T) -> S?
): List<S> = parallelMap(maxConcurrentRequests, mapper).filterNotNull()

/**
 * Maps every element of this collection using [mapper] in parallel and filters out `null`.
 *
 * @param maxConcurrentRequests the maximum amount of concurrent coroutines (`null` means unlimited)
 */
public suspend fun <T, S> Collection<T>.parallelMapNotNullIndexed(
    maxConcurrentRequests: Int? = null,
    mapper: suspend (index: Int, T) -> S?
): List<S> = parallelMapIndexed(maxConcurrentRequests, mapper).filterNotNull()

/**
 * Maps every element of this collection using [mapper] in parallel.
 *
 * @param maxConcurrentRequests the maximum amount of concurrent coroutines (`null` means unlimited)
 */
public suspend fun <T, S> Collection<T>.parallelMapIndexed(
    maxConcurrentRequests: Int? = null,
    mapper: suspend (index: Int, T) -> S
): List<S> {
    val result = ArrayList<S>(size)

    val semaphore = maxConcurrentRequests?.let { Semaphore(it) }

    coroutineScope {
        forEachIndexed { index, item ->
            launch {
                val block = suspend {
                    val found = mapper(index, item)
                    if (found != null) {
                        result.add(index, found)
                    }
                }

                if (semaphore != null) {
                    semaphore.withPermit { block() }
                } else {
                    block()
                }
            }
        }
    }

    return result
}
