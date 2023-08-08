package dev.schlaubi.mikbot.plugin.api.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

/**
 * Coroutine dispatcher using [Executors.newVirtualThreadPerTaskExecutor] for dispatching.
 */
public val loomDispatcher: CoroutineDispatcher = Executors.newVirtualThreadPerTaskExecutor()
    .asCoroutineDispatcher()

/**
 * Executes the oncoming block using [loomDispatcher].
 */
public suspend fun <T> blocking(block: suspend CoroutineScope.() -> T): T = withContext(loomDispatcher, block)
