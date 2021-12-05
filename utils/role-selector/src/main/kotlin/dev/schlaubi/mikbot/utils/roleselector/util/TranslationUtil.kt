package dev.schlaubi.mikbot.utils.roleselector.util

import com.kotlindiscord.kord.extensions.commands.CommandContext

suspend fun CommandContext.translateString(key: String, vararg arguments: Any?) =
    translate(
        key = key,
        bundleName = "roleselector",
        replacements = arguments as Array<Any?>
    )