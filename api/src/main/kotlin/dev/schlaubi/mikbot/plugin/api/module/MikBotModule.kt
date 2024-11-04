package dev.schlaubi.mikbot.plugin.api.module

import dev.kordex.core.extensions.Extension
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.Plugin

/**
 * Implementation of [Extension] which stores a [PluginContext].
 *
 * @property context the [PluginContext] which registered this module
 * @see Plugin.add
 */
public abstract class MikBotModule(public val context: PluginContext) : Extension()
