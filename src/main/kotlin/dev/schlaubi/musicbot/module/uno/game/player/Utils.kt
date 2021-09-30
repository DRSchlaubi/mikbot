package dev.schlaubi.musicbot.module.uno.game.player

import dev.schlaubi.musicbot.core.io.findUser

@Suppress("UNCHECKED_CAST")
suspend fun DiscordUnoPlayer.translate(key: String, vararg replacements: Any?) =
    game.translationsProvider.translate(
        key,
        game.database.users.findUser(owner).language,
        "uno",
        replacements = replacements as Array<Any?>
    )
