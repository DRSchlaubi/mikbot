package dev.schlaubi.musicbot.module.uno

import dev.kord.common.entity.Snowflake
import dev.schlaubi.musicbot.module.uno.game.DiscordUnoGame

private val unoGames = mutableMapOf<Snowflake, DiscordUnoGame>()

fun registerUno(id: Snowflake, uno: DiscordUnoGame) {
    unoGames[id] = uno
}

fun unregisterUno(id: Snowflake) = unoGames.remove(id)

fun findUno(id: Snowflake) = unoGames[id]
