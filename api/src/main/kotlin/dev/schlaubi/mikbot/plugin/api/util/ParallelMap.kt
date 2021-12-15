package dev.schlaubi.mikbot.plugin.api.util

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

public suspend fun <T, S> Collection<T>.parallelMapNotNull(
    maxConcurrentRequests: Int? = null,
    mapper: suspend (T) -> S?
): List<S> = parallelMapNotNullIndexed(maxConcurrentRequests) { _, t -> mapper(t) }

public suspend fun <T, S> Collection<T>.parallelMapNotNullIndexed(
    maxConcurrentRequests: Int? = null,
    mapper: suspend (index: Int, T) -> S?
): List<S> {
    val result = MutableList<S?>(size) { null }

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

    return result.filterNotNull()
}
