package dev.schlaubi.mikbot.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class InstallBotTask : DefaultTask() {
    @get:OutputDirectory
    internal val testBotFolder: Provider<Directory>
        get() = project.layout.buildDirectory.dir("test-bot/${botVersionFromProject()}")

    @get:Input
    @get:Optional
    abstract val botVersion: Property<String>

    @get:Inject
    abstract val fs: FileSystemOperations
    @get:Inject
    abstract val archives: ArchiveOperations

    private val runtimeDependency = project.dependencies.create(
        mapOf(
            "name" to "bot",
            "version" to botVersionFromProject(),
            "ext" to "tar.gz"
        )
    )

    private val configuration = project.configurations.detachedConfiguration(runtimeDependency)

    @TaskAction
    fun runTask() {
        if (botVersion.isPresent && botVersion.get() != botVersionFromProject()) {
            logger.warn("Install task botVersion differs from dependency, please use either the mikbot() dependency or the installTask bot version")
        }

        val archive = downloadMikbot()
        extractBot(archive)
    }

    private fun downloadMikbot(): File = configuration.resolve().single()

    private fun extractBot(archive: File) {
        fs.copy {
            from(archives.tarTree(archive))
            into(testBotFolder)
        }
    }

    internal fun botVersionFromProject(): String =
        botVersion.orNull ?: project.extractMikBotVersionFromProjectApiDependency()
}
