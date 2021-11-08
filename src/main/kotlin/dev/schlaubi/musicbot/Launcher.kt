package dev.schlaubi.musicbot

import ch.qos.logback.classic.Logger
import dev.schlaubi.mikbot.plugin.api._pluginSystem
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.musicbot.core.Bot
import dev.schlaubi.musicbot.core.plugin.PluginLoader
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString

suspend fun main() {
    initializeLogging()
    loadPlugins()
    Bot().start()
}

private fun loadPlugins() {
    System.setProperty("pf4j.pluginsDir", Config.PLUGIN_PATH.absolutePathString())
    _pluginSystem = PluginLoader.system
    PluginLoader.loadPlugins()
    PluginLoader.startPlugins()
}

private fun initializeLogging() {
    val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    rootLogger.level = Config.LOG_LEVEL
}
