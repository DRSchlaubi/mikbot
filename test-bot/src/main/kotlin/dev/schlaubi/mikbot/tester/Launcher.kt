package dev.schlaubi.mikbot.tester

import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import dev.schlaubi.musicbot.main as botMain

suspend fun main() {
    val pluginsDirectory = Path("test-bot", "plugins")
    System.setProperty("pf4j.pluginsDir", pluginsDirectory.absolutePathString())

    botMain()
}
