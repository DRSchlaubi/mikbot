package dev.schlaubi.musicbot

import ch.qos.logback.classic.Logger
import dev.schlaubi.mikbot.plugin.api._pluginSystem
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.musicbot.core.Bot
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

suspend fun main() {
    initializeLogging()
    val bot = Bot()
    loadPlugins(bot)
    bot.start()
}

private fun loadPlugins(bot: Bot) {
    if (System.getProperty("pf4j.pluginsDir").isNullOrBlank()) {
        System.setProperty("pf4j.pluginsDir", Config.PLUGIN_PATH.absolutePathString())
    }

    if (!Config.PLUGIN_PATH.exists()) {
        Config.PLUGIN_PATH.createDirectories()
    }

    _pluginSystem = bot.pluginSystem
    bot.pluginLoader.loadPlugins()
    bot.pluginLoader.startPlugins()
}

private fun initializeLogging() {
    val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as? Logger
    if (rootLogger == null) {
        LoggerFactory.getLogger("MikBot").warn("Could not set log level due to different logging engine being used")
        return
    }
    rootLogger.level = Config.LOG_LEVEL
}
