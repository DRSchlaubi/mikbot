package dev.schlaubi.mikbot.plugin.api.util

import dev.kordex.core.checks.types.CheckContext
import dev.kord.core.event.Event

/**
 * Only executes this series of checks if the context is still passing.
 */
public inline fun <T : Event> CheckContext<T>.ifPassing(block: CheckContext<T>.() -> Unit) {
    if (passed) block()
}
