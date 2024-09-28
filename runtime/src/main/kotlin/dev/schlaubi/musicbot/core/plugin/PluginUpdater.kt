package dev.schlaubi.musicbot.core.plugin

import dev.schlaubi.mikbot.plugin.api.config.Config
import mu.KotlinLogging
import org.pf4j.update.*
import org.pf4j.update.verifier.BasicVerifier
import kotlin.io.path.absolutePathString
import kotlin.io.path.moveTo

private val LOG = KotlinLogging.logger { }

class PluginUpdater(private val pluginLoader: PluginLoader, repos: List<UpdateRepository>) :
    UpdateManager(pluginLoader, repos) {
    override fun getFileVerifier(pluginId: String?): FileVerifier =
        if (Config.VALIDATE_CHECKSUMS) super.getFileVerifier(pluginId) else BasicVerifier()

    override fun getFileDownloader(pluginId: String?): FileDownloader = KtorHttpFileDownloader

    internal fun checkForUpdates() {
        if (repositories.isEmpty()) {
            LOG.warn { "No plugin repositories are defined, Updater will disable itself" }
            return
        }

        downloadRequestedPlugins()

        if (Config.UPDATE_PLUGINS) {
            updatePlugins()
        }
    }


    private fun updatePlugins() {
        if (hasUpdates()) {
            val updates = updates
            LOG.debug { "Found ${updates.size} plugin updates" }
            for (plugin in updates) {
                LOG.debug { "Found update for plugin '${plugin.id}'" }
                val lastRelease = getLastPluginRelease(plugin.id)
                // Don't update plugins which a specific version requested
                if (Config.DOWNLOAD_PLUGINS.firstOrNull { it.id == plugin.id }?.version?.equals(lastRelease.version) == false) {
                    continue
                }
                val lastVersion = lastRelease.version
                val installedPlugin = pluginLoader.getPlugin(plugin.id)
                if (installedPlugin == null || BUNDLED_PLUGINS in installedPlugin.pluginPath.absolutePathString()) continue
                val installedVersion = installedPlugin.descriptor.version

                LOG.debug { "Update plugin '${plugin.id}' from version $installedVersion to version $lastVersion" }
                val updated = updatePlugin(plugin.id, lastVersion)
                if (updated) {
                    LOG.debug { "Updated plugin '${plugin.id}'" }
                } else {
                    LOG.error { "Cannot update plugin '${plugin.id}'" }
                }
            }
        } else {
            LOG.debug { "No updates found" }
        }
    }

    private fun downloadRequestedPlugins() {
        if (hasAvailablePlugins()) {
            val installedPlugins = pluginLoader.plugins.map { it.pluginId }
            val requestedPlugins = Config.DOWNLOAD_PLUGINS
            val availablePlugins = availablePlugins
                .associateBy(PluginInfo::id)

            if (requestedPlugins.isNotEmpty()) {
                LOG.info { "Attempting to download the following plugins: $requestedPlugins" }
            }

            requestedPlugins.forEach {
                val plugin = availablePlugins[it.id]
                if (plugin == null) {
                    LOG.warn { "Could not find plugin ${it.id} in any repo" }
                    return@forEach
                }

                val version = it.version ?: getLastPluginRelease(plugin.id).version

                if (plugin.id in installedPlugins) {
                    val installedPlugin = pluginLoader.getPlugin(plugin.id)
                    if (installedPlugin.descriptor.version != version) {
                        LOG.info { "Downgrading plugin ${plugin.id} to $version" }
                        updatePlugin(plugin.id, version)
                    }
                } else {
                    val downloaded = downloadPlugin(plugin.id, version)
                    val destination = pluginLoader.pluginsRoot.resolve(downloaded.fileName)
                    downloaded.moveTo(destination)
                    LOG.info { "Downloaded plugin ${plugin.id} $version" }
                    pluginLoader.loadPlugin(destination)
                }
            }
        }
    }
}
