package dev.schlaubi.mikbot.plugin.api

import dev.kordex.core.builders.AboutBuilder
import org.pf4j.ExtensionPoint

/**
 * Allows to modify the about command.
 */
public interface AboutExtensionPoint : ExtensionPoint {
    /**
     * Applies this extensions settings to the about page.
     */
    public suspend fun AboutBuilder.apply()
}
