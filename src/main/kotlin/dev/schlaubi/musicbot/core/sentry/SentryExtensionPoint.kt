package dev.schlaubi.musicbot.core.sentry

import io.sentry.Breadcrumb
import io.sentry.Hint
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import org.pf4j.ExtensionPoint

/**
 * Allows plugins to customize sentry.
 */
interface SentryExtensionPoint : ExtensionPoint {
    /**
     * Apply custom sentry options in the setup.
     *
     * DO NOT SET [SentryOptions.beforeSend] or [SentryOptions.beforeBreadcrumb] here!
     * IT WON'T WORK
     */
    fun SentryOptions.setup()

    /**
     * Allows plugins to listen to the beforeSend Sentry event.
     */
    fun beforeSend(sentryEvent: SentryEvent, hint: Hint)

    /**
     * Allows plugins to listen to the beforeBreadcrumb Sentry event.
     */
    fun beforeBreadcrumb(breadcrumb: Breadcrumb, hint: Hint)
}
