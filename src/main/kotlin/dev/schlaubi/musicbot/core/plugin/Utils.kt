package dev.schlaubi.musicbot.core.plugin

import dev.schlaubi.mikbot.plugin.api.Plugin
import org.pf4j.Plugin as PF4JPlugin

fun PF4JPlugin.asPlugin(): Plugin = this as? Plugin ?: error("Invalid plugin detected: $this")
