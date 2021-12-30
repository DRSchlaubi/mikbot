package dev.schlaubi.mikbot.tester

import dev.schlaubi.mikbot.plugin.api.util.ensurePath
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText
import dev.schlaubi.musicbot.main as botMain

suspend fun main() {
    val projectPathFile = Path(ClassLoader.getSystemClassLoader().getResource("bot-project-path.txt")!!.file.ensurePath())
    val projectPath = Path(projectPathFile.readText())

    System.setProperty("pf4j.pluginsDir", projectPath.absolutePathString())

    botMain()
}
