package dev.schlaubi.mikbot.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists

abstract class InstallBotTask : DefaultTask() {
    @get:Internal
    internal val testBotFolder: Path
        get() = project.buildDir.resolve("test-bot").resolve(botVersion.get()).toPath()
    private val botArchive: Path
        get() = testBotFolder.resolve("bot.tar.gz")

    @get:Input
    abstract val botVersion: Property<String>

    @TaskAction
    fun runTask() {
        if (testBotFolder.exists()) {
            // did work is the most supid name ever
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

        val version = botVersion.orNull ?: error("Please set a bot version")
        val url = "https://github.com/DRSchlaubi/mikbot/raw/binary-repo/$version/mikmusic-$version.tar.gz"
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
}
