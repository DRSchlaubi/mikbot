package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.core.event.Event

/**
 * Only executes this series of checks if the context is still passing.
 */
inline fun <T : Event> CheckContext<T>.ifPassing(block: CheckContext<T>.() -> Unit) {
    if (passed) block()
}
