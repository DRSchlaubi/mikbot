package dev.schlaubi.musicbot

import ch.qos.logback.classic.Logger
import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.core.MusicBot
import io.sentry.Sentry
import io.sentry.SentryOptions
import org.slf4j.LoggerFactory

suspend fun main() {
    initializeLogging()
    initializeSentry()
    MusicBot().start()
}

private fun initializeLogging() {
    val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    rootLogger.level = Config.LOG_LEVEL
}

private fun initializeSentry() {
    val configure: (SentryOptions) -> Unit =
        if (Config.ENVIRONMENT.useSentry) {
            { it.dsn = Config.SENTRY_TOKEN }
        } else {
            { it.dsn = "" }
        }

    Sentry.init(configure)
}
