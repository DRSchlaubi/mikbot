package dev.schlaubi.mikbot.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists

const val mikbotBinaryUrl = "https://storage.googleapis.com/mikbot-binaries/"

abstract class InstallBotTask : DefaultTask() {
    @get:Internal
    internal val testBotFolder: Path
        get() = project.buildDir.resolve("test-bot").resolve(botVersionFromProject()).toPath()
    private val botArchive: Path
        get() = testBotFolder.resolve("bot.tar.gz")

    @get:Input
    @get:Optional
    abstract val botVersion: Property<String>

    @TaskAction
    fun runTask() {
        if (botVersion.isPresent && botVersion.get() != botVersionFromProject()) {
            logger.warn("Install task botVersion differs from dependency, please use either the mikbot() dependency or the installTask bot version")
        }
        if (!project.gradle.startParameter.isRerunTasks && testBotFolder.exists()) {
            // did work is the most stupid name ever
            // it actually means whether the task did some work
            // not whether the task worked
            didWork = false
            return
        }
        downloadMikbot()
        extractBot()
    }

    private fun downloadMikbot() {
        if (!testBotFolder.exists()) {
            testBotFolder.createDirectories()
        }
        logger.info("Mikbot installation not found! Downloading")

        val version = botVersionFromProject()
        val url = "$mikbotBinaryUrl$version/mikmusic-$version.tar.gz"
        downloadBot(url)
    }

    private fun extractBot() {
        project.copy {
            with(it) {
                from(project.tarTree(botArchive.toAbsolutePath()))
                into(testBotFolder)
            }
        }

        project.delete {
            it.delete(botArchive.toAbsolutePath())
        }
    }


    private fun downloadBot(url: String) {
        if (!botArchive.exists()) {
            botArchive.createFile()
        }
        val website = URL(url)
        val readChannel = Channels.newChannel(website.openStream())
        val writeChannel = FileChannel.open(botArchive, StandardOpenOption.WRITE)
        writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE)
    }

    internal fun botVersionFromProject(): String {
        return botVersion.orNull ?: project.extractMikBotVersionFromProjectApiDependency()
        ?: error("Unable to detect version. Fix your dependency or set it manually.")
    }
}
