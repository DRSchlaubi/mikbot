package dev.schlaubi.mikbot.gradle

import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@Suppress("LeakingThis")
abstract class AssembleBotTask : Zip() {

    @get:Internal
    internal abstract val installBotTask: Property<InstallBotTask>

    @get:Internal
    internal abstract val assemblePlugin: Property<Zip>

    /**
     * List of repositories used to download [bundledPlugins].
     */
    @get:Input
    abstract val repositories: ListProperty<String>

    /**
     * List of plugin specs that will be bundled (this does not support version ranges).
     *
     * Example: `gdpr@1.0.0`
     */
    @get:Input
    abstract val bundledPlugins: ListProperty<String>

    private val client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build()

    private val pluginSpecs = bundledPlugins.map {
        it.map { plugin ->
            val split = plugin.split('@')
            if (split.size != 2) {
                error("Invalid name $plugin, please specify as id@version ")
            }

            val (id, version) = split

            id to version
        }
    }

    init {
        group = "mikbot"
        repositories.convention(emptyList())
        bundledPlugins.convention(emptyList())
    }

    internal fun config() {
        destinationDirectory = project.layout.buildDirectory.dir("bot")
        archiveBaseName = "bot-${project.name}"
        archiveExtension = "zip"

        into("") {
            // make this lazy, so it doesn't throw at initialization
            val provider = project.provider {
                val version = installBotTask.get().botVersionFromProject()
                installBotTask.get().testBotFolder.get().dir("bot-$version")
            }
            from(provider)
        }
        val installedPluginsName = "lib/bundled-plugins"
        into(installedPluginsName) {
            val provider = assemblePlugin.flatMap { task -> task.archiveFile }
            from(provider)
        }
        into(installedPluginsName) {
            pluginSpecs.get().forEach { (id, version) ->
                val fullPath = project.pluginCache.get().dir("$id/$version")
                from(fullPath)
                include("*.zip")
            }
        }
    }

    override fun copy() = runBlocking {
        downloadPlugins()
        super.copy()
    }

    @Suppress("ConvertLambdaToReference")
    private fun downloadPlugins() {
        logger.debug("Building plugin repo map")

        val repositoryUrls = repositories.get() + "https://storage.googleapis.com/mikbot-plugins"

        @Suppress("ConvertLambdaToReference")
        val pluginInfos = runBlocking {
            repositoryUrls
                .asSequence()
                .map {
                    HttpRequest.newBuilder(URI.create("$it/plugins.json"))
                        .build()
                }
                .map {
                    async {
                        val response = client
                            .sendAsync(it, KotlinxSerializationBodyHandler<List<PluginInfo>>())
                            .await()
                        if (response.statusCode() != 200) {
                            null
                        } else {
                            response.body()
                        }
                    }
                }
                .toList()
                .awaitAll()
                .asSequence()
                .filterNotNull()
                .flatMap { it.asSequence() }
                .toList()
        }

        val pluginReleases = pluginSpecs.get()
            .mapNotNull {(id, version) ->
                val pluginInfo = pluginInfos.firstOrNull { (pluginId) -> pluginId == id } ?: run {
                    logger.warn("Could not find plugin $id in any repo")
                    return@mapNotNull null
                }
                val release = pluginInfo.releases.firstOrNull { (pluginVersion) ->
                    pluginVersion == version
                } ?: run {
                    logger.warn("Could not find plugin release $version of $id in any repo")
                    return@mapNotNull null
                }

                PluginPair(id, version, release.url)
            }
        runBlocking {
            pluginReleases
                .asSequence()
                .mapNotNull {
                    val destination = it.cachePath.asFile.toPath()
                    if (!destination.exists()) {
                        destination.parent.createDirectories()

                        destination to HttpRequest.newBuilder(URI.create(it.url)).build()
                    } else {
                        null
                    }
                }
                .map { (destination, request) ->
                    launch {
                        logger.quiet("Download ${request.uri()}")
                        client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(destination)).await()
                    }
                }
                .toList()
                .joinAll()

        }
    }

    private val PluginPair.cachePath: Directory
        get() = project.pluginCache.get().dir("$id/$version/plugin-${id}-${version}.zip")
}

data class PluginPair(val id: String, val version: String, val url: String)

private val Project.pluginCache: Provider<Directory>
    get() = layout.buildDirectory.dir("plugin-cache")


