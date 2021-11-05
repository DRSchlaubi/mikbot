package dev.schlaubi.mikmusic.util

import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.schlaubi.mikmusic.core.MusicModule

val Extension.musicModule: MusicModule get() = bot.findExtension<MusicModule>()!!
