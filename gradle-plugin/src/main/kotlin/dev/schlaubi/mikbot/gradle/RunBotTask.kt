package dev.schlaubi.mikbot.gradle

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
 import org.gradle.kotlin.dsl.*
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.notExists
import kotlin.io.path.readLines

abstract class RunBotTask : JavaExec() {
    @get:Internal
    internal abstract val installTask: Property<InstallBotTask>

    init {
        mainClass = "dev.schlaubi.musicbot.LauncherKt"
        outputs.upToDateWhen { false } // always start the bot
    }

    @TaskAction
    override fun exec() {
        configureSystemProperties()
        workingDir(installTask.get().testBotFolder)
        configureClasspath()
        configureEnvironment()
        super.exec()
    }

    private fun configureEnvironment() {
        val envFile = project.file(".test-env").toPath()
        if (envFile.notExists() || envFile.isDirectory()) {
            return
        }
        envFile.readLines()
            .asSequence()
            .filter(String::isNotBlank)
            .map(String::trimStart)
            .filterNot { it.startsWith('#') }
            .forEach {
                val (key, value) = it.split('=')
                environment[key] = value.trimEnd()
            }
    }

    private fun configureClasspath() {
        val installTask = installTask.get()
        val folder =
            installTask.testBotFolder.dir("bot-${installTask.botVersionFromProject()}")
                .dir("lib")
        val jars = folder.get().asFile.toPath().listDirectoryEntries("*.jar")

        classpath += objectFactory.fileCollection().from(jars)
    }

    private fun configureSystemProperties() {
        systemProperties["pf4j.pluginsDir"] = project.layout.buildDirectory.dir("test-bot/plugins")
    }
}
