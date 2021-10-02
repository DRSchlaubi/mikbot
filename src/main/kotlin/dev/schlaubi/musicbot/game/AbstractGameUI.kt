package dev.schlaubi.musicbot.game

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.schlaubi.musicbot.module.uno.game.joinGameButton
import dev.schlaubi.musicbot.module.uno.game.startGameButton

const val leaveGameButton = "leave_game"

fun ActionRowBuilder.leaveButton(text: String) = interactionButton(ButtonStyle.Danger, leaveGameButton) {
    label = text
}

fun MessageModifyBuilder.gameUI(game: AbstractGame<*>) {
    actionRow {
        if (!game.running) {
            interactionButton(ButtonStyle.Success, joinGameButton) {
                label = "Join Game"
            }

            interactionButton(ButtonStyle.Primary, startGameButton) {
                label = "Start Game"
            }
        }

        leaveButton("Leave Game")
    }
}
