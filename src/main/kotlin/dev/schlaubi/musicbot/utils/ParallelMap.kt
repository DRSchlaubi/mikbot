package dev.schlaubi.musicbot.utils

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun <T, S> Collection<T>.parallelMapNotNull(mapper: suspend (T) -> S?): ArrayList<S> {
    val result = ArrayList<S>(size)

    var exception: Throwable? = null
    coroutineScope {
        forEach {
            launch {
                try {
                    mapper(it)?.let { item ->
                        result.add(item)
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
