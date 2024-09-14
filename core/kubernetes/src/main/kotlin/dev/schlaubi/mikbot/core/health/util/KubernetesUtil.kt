package dev.schlaubi.mikbot.core.health.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

private val dispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

suspend fun <T> blocking(block: suspend CoroutineScope.() -> T) = withContext(dispatcher, block)
