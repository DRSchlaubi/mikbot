package dev.schlaubi.musicbot.core.plugin

import dev.schlaubi.mikbot.plugin.api.config.Config
import mu.KotlinLogging
import org.pf4j.DependencyResolver
import org.pf4j.PluginDescriptor

private val LOG = KotlinLogging.logger { }

internal fun PluginLoader.checkForUpdates() {
    if (repos.isEmpty()) {
        LOG.warn { "No plugin repositories are defined, Updater will disable itself" }
        return
    }

    updatePlugins()

    downloadRequestedPlugins()

    plugins.forEach {
        val exception = it.failedException
        if (exception is DependencyResolver.DependenciesNotFoundException) {
            if (exception.dependencies.all { id ->
                    getPlugin(id) != null
                }) {

                attemptPluginRestart(it.descriptor)
            }
        }

        if (exception is DependencyResolver.DependenciesWrongVersionException) {
            if (exception.dependencies.all { dependency ->
                    getPlugin(dependency.dependencyId)?.descriptor?.version == dependency.requiredVersion
                }) {

                attemptPluginRestart(it.descriptor)
            }
        }
    }
}

private fun PluginLoader.attemptPluginRestart(descriptor: PluginDescriptor) {
    val result = resolver.resolve(listOf(descriptor))
    if (result.hasCyclicDependency() || result.notFoundDependencies.isNotEmpty() || result.wrongVersionDependencies.isNotEmpty()) {
        LOG.warn { "Restart of plugin ${descriptor.pluginId} failed because of invalid dependencies" }
    }
    startPlugin(descriptor.pluginId)
}

private fun PluginLoader.updatePlugins() {
    if (updateManager.hasUpdates()) {
        val updates = updateManager.updates
        LOG.debug { "Found ${updates.size} plugin updates" }
        for (plugin in updates) {
            LOG.debug { "Found update for plugin '${plugin.id}'" }
            val lastRelease = updateManager.getLastPluginRelease(plugin.id)
            // Don't update plugins which a specific version requested
            if (Config.DOWNLOAD_PLUGINS.firstOrNull { it.id == plugin.id }?.version?.equals(lastRelease.version) == false)
                continue
            val lastVersion = lastRelease.version
            val installedVersion = getPlugin(plugin.id).descriptor.version

            LOG.debug { "Update plugin '${plugin.id}' from version $installedVersion to version $lastVersion" }
            val updated = updateManager.updatePlugin(plugin.id, lastVersion)
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

private fun PluginLoader.downloadRequestedPlugins() {
    if (updateManager.hasAvailablePlugins()) {
        val installedPlugins = plugins.map { it.pluginId }
        val requestedPlugins = Config.DOWNLOAD_PLUGINS
        val availablePlugins = updateManager.availablePlugins
            .associateBy { it.id }

        if (requestedPlugins.isNotEmpty()) {
            LOG.info { "Attempting to download the following plugins: $requestedPlugins" }
        }

        requestedPlugins.forEach {
            val plugin = availablePlugins[it.id]
            if (plugin == null) {
                LOG.warn { "Could not find plugin ${it.id} in any repo" }
                return@forEach
            }

            val version = it.version ?: updateManager.getLastPluginRelease(plugin.id).version

            if (plugin.id in installedPlugins) {
                val installedPlugin = getPlugin(plugin.id)
                if (installedPlugin.descriptor.version != version) {
                    LOG.info { "Downgrading plugin ${plugin.id} to ${version}" }
                    updateManager.updatePlugin(plugin.id, version)
                }
            } else {
                val installed = updateManager.installPlugin(plugin.id, version)
                if (installed) {
                    LOG.info { "Successfully installed plugin ${plugin.id} from a repository" }
                } else {
                    LOG.info { "Installation for plugin ${plugin.id} failed" }
                }
            }
        }
    }
}
