package dev.schlaubi.mikbot.gradle

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.notExists
import kotlin.io.path.readLines

abstract class RunBotTask : JavaExec() {
    @get:Internal
    internal abstract val installTask: Property<InstallBotTask>

    init {
        mainClass.set("dev.schlaubi.musicbot.LauncherKt")
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
        envFile.readLines().forEach {
            if (it.isNotBlank()) {
                val (key, value) = it.split('=')
                environment[key] = value.trimEnd()
            }
        }
    }

    private fun configureClasspath() {
        val installTask = installTask.get()
        val folder = installTask.testBotFolder.resolve("mikmusic-${installTask.botVersion.get()}").resolve("lib")
        val jars = folder.listDirectoryEntries("*.jar")

        classpath += objectFactory.fileCollection().from(jars)
    }

    private fun configureSystemProperties() {
        systemProperties["pf4j.pluginsDir"] = project.buildDir.resolve("test-bot").resolve("plugins")
    }
}
