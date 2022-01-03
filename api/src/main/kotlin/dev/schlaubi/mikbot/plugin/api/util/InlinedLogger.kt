package dev.schlaubi.mikbot.plugin.api.util

import mu.KLogger

/**
 * Lazy producer of a log message.
 */
public typealias LazyLogMessage = () -> String

/**
 * Inline version of [KLogger.debug] so it can call suspend calls.
 */
public inline fun KLogger.debugInlined(message: LazyLogMessage) {
    if (isDebugEnabled) {
        debug(message())
    }
}

/**
 * Inline version of [KLogger.trace] so it can call suspend calls.
 */
public inline fun KLogger.traceInlined(message: LazyLogMessage) {
    if (isTraceEnabled) {
        trace(message())
    }
}

/**
 * Inline version of [KLogger.error] so it can call suspend calls.
 */
public inline fun KLogger.errorInlined(message: LazyLogMessage) {
    if (isErrorEnabled) {
        error(message())
    }
}

/**
 * Inline version of [KLogger.info] so it can call suspend calls.
 */
public inline fun KLogger.infoInlined(message: LazyLogMessage) {
    if (isInfoEnabled) {
        error(message())
    }
}

/**
 * Inline version of [KLogger.warn] so it can call suspend calls.
 */
public inline fun KLogger.warnInlined(message: LazyLogMessage) {
    if (isWarnEnabled) {
        warn(message())
    }
}
