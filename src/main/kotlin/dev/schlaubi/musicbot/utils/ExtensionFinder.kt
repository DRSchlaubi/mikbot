package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.extensions.Extension

inline fun <reified T> Extension.extension(): Lazy<T> = lazy { bot.findExtension<T>()!! }
