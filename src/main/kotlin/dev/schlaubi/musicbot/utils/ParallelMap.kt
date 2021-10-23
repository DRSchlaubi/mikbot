package dev.schlaubi.musicbot.utils

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

suspend fun <T, S> Collection<T>.parallelMapNotNull(
    maxConcurrentRequests: Int? = null,
    mapper: suspend (T) -> S?
): ArrayList<S> = parallelMapNotNullIndexed(maxConcurrentRequests) { _, t -> mapper(t) }

suspend fun <T, S> Collection<T>.parallelMapNotNullIndexed(
    maxConcurrentRequests: Int? = null,
    mapper: suspend (index: Int, T) -> S?
): ArrayList<S> {
    val result = ArrayList<S>(size)

    val semaphore = maxConcurrentRequests?.let { Semaphore(it) }

    var exception: Throwable? = null
    coroutineScope {
        forEachIndexed { index, item ->
            launch {
                val block = suspend {
                    val found = mapper(index, item)
                    if (found != null) {
                        result.add(found)
                    }
                }

                try {
                    if (semaphore != null) {
                        semaphore.withPermit { block() }
                    } else {
                        block()
                    }
                } catch (e: Throwable) {
                    cancel()
                    exception = e
                }
            }
        }
    }

    if (exception != null) {
        throw exception!!
    }
    return result
}
