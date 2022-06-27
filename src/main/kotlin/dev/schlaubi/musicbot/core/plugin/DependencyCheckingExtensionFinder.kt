package dev.schlaubi.musicbot.core.plugin

import mu.KotlinLogging
import org.pf4j.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private val LOG = KotlinLogging.logger { }

/**
 * Implementation of [ExtensionFinder] detecting dependencies automatically.
 *
 * This assumes that one extension only extends one [ExtensionPoint] by skipping all dependencies throwing [NoClassDefFoundErrors][NoClassDefFoundError] during class loading.
 */
class DependencyCheckingExtensionFinder(pluginManager: PluginManager) : LegacyExtensionFinder(pluginManager) {
    override fun <T : Any> find(type: Class<T>, pluginId: String?): List<ExtensionWrapper<T>> {
        val kType = type.kotlin
        LOG.debug {
            "Finding extensions of extension point '${kType.qualifiedName}' for plugin '$pluginId'"
        }
        val result = buildList {
            // classpath's extensions <=> pluginId = null
            val classNames = findClassNames(pluginId)
            if (classNames.isNullOrEmpty()) {
                return@buildList
            }
            if (pluginId != null) {
                val pluginWrapper = pluginManager.getPlugin(pluginId)
                if (PluginState.STARTED != pluginWrapper.pluginState && PluginState.RESOLVED != pluginWrapper.pluginState) {
                    return@buildList
                }
                LOG.trace { "Checking extensions from plugin '$pluginId'" }
            } else {
                LOG.trace { "Checking extensions from classpath" }
            }
            val classLoader =
                if (pluginId != null) pluginManager.getPluginClassLoader(pluginId) else javaClass.classLoader
            classNames.forEach { className ->
                try {
                    val extensionClass = try {
                        classLoader.loadClass(className).kotlin
                    } catch (e: NoClassDefFoundError) {
                        LOG.warn { "Could not load extension $className, because a missing plugin dependency" }
                        LOG.debug(e::stackTraceToString)
                        return@forEach
                    }
                    LOG.debug { "Checking extension type '$className'" }
                    if (extensionClass.isSubclassOf(kType)) {
                        val extensionWrapper = createExtensionWrapper(extensionClass)
                        @Suppress("UNCHECKED_CAST")
                        add(extensionWrapper as ExtensionWrapper<T>)
                        LOG.debug {
                            "Added extension '$className' with ordinal ${extensionWrapper.ordinal}"
                        }
                    } else {
                        LOG.trace { "'$className' is not an extension for extension point '${type.name}'" }
                    }
                } catch (e: ClassNotFoundException) {
                    LOG.error(e.message, e)
                }
            }
        }

        return result.sorted()
    }

    private fun createExtensionWrapper(extensionClass: KClass<*>): ExtensionWrapper<*> {
        val extensionAnnotation = findExtensionAnnotation(extensionClass.java)
        val ordinal = extensionAnnotation?.ordinal ?: 0
        val descriptor = ExtensionDescriptor(ordinal, extensionClass.java)
        return ExtensionWrapper<Any>(descriptor, pluginManager.extensionFactory)
    }
}
