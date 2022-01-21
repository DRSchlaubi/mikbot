package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.core.entity.interaction.OptionValue

public val OptionValue<*>.safeInput: String get() = value?.toString() ?: ""
