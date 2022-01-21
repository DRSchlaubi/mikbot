package dev.schlaubi.mikbot.game.trivia.translate

import com.google.api.core.ApiFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Suspends an [ApiFuture].
 */
suspend fun <T> ApiFuture<T>.await(): T {
    // fast path when ApiFuture is already done (does not suspend)
    if (isDone) {
        try {
            @Suppress("BlockingMethodInNonBlockingContext")
            return get()
        } catch (e: ExecutionException) {
            throw e.cause ?: e // unwrap original cause from ExecutionException
        }
    }

    // slow path -- suspend
    return suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
        this.addListener(
            {
                try {
                    cont.resume(get())
                } catch (e: ExecutionException) {
                    cont.resumeWithException(e.cause ?: e) // unwrap original cause from ExecutionException
                }
            },
            MoreExecutors.directExecutor()
        )
        cont.invokeOnCancellation {
            cancel(false)
        }
    }
}
