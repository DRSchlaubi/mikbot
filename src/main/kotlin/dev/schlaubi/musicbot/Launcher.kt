package dev.schlaubi.musicbot

import ch.qos.logback.classic.Logger
import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.core.MusicBot
import org.slf4j.LoggerFactory

suspend fun main() {
    initializeLogging()
    MusicBot().start()
}

private fun initializeLogging() {
    val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    rootLogger.level = Config.LOG_LEVEL
}
