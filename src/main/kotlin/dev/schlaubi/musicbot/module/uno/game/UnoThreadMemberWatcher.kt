package dev.schlaubi.musicbot.module.uno.game

import dev.kord.core.behavior.interaction.edit
import dev.schlaubi.musicbot.module.uno.game.player.DiscordUnoPlayer
import dev.schlaubi.musicbot.module.uno.game.player.translate

suspend fun DiscordUnoGame.kickPlayer(player: DiscordUnoPlayer) {
    runCatching {
        player.controls.edit {
            components = mutableListOf()
            content = player.translate("uno.controls.left")
        }
    }

    game.removePlayer(player)

    // Cancel turn for current player if it is the leaving player or,
    // there are no players left (end game)
    if (currentPlayer == player || players.size <= 1) {
        game.skipPlayer() // leaving confuses the player sequence and lets the left player play again
        currentTurn?.cancel()
    }
}
