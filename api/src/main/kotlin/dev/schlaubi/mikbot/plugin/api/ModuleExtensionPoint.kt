package dev.schlaubi.mikbot.plugin.api

import com.kotlindiscord.kord.extensions.extensions.Extension
import org.pf4j.ExtensionPoint
import kotlin.reflect.KClass

/**
 * Extension point for the module [T].
 */
public interface ModuleExtensionPoint<T : Extension> : ExtensionPoint {
    /**
     * Applies instructions to the module.
     */
    public suspend fun T.apply()
}

@InternalAPI
public abstract class ModuleExtensionPointImpl<T : Extension> : Extension() {
    protected abstract val extensionClazz: KClass<out ModuleExtensionPoint<T>>

    @Suppress("UNCHECKED_CAST", "RedundantModalityModifier")
    open override suspend fun setup() {
        pluginSystem.getExtensions(extensionClazz).forEach {
            with(it) {
                (this@ModuleExtensionPointImpl as T).apply()
            }
        }
    }
}
