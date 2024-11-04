package dev.schlaubi.mikmusic.util

import dev.kordex.core.extensions.Extension
import dev.schlaubi.mikmusic.core.MusicModule

val Extension.musicModule: MusicModule get() = bot.findExtension<MusicModule>()!!
