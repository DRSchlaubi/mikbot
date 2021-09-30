package dev.schlaubi.musicbot.module.uno.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.musicbot.module.uno.UnoModule
import dev.schlaubi.musicbot.module.uno.game.DiscordUnoGame
import dev.schlaubi.musicbot.module.uno.game.joinGameButton
import dev.schlaubi.musicbot.module.uno.game.leaveButton
import dev.schlaubi.musicbot.module.uno.game.startGameButton

fun UnoModule.startGameCommand() = publicSubCommand {
    name = "start"
    description = "Starts a new game"

    action {
        val gameThread = textChannel.startPublicThread("uno-game")
        gameThread.addUser(user.id) // Add creator
        val gameMessage = gameThread.createMessage {
            embed {
                title = "UNO game"

                footer {
                    text = "Leave this thread or click leave to leave the game"
                }
            }

            actionRow {
                interactionButton(ButtonStyle.Success, joinGameButton) {
                    label = "Join"
                }

                interactionButton(ButtonStyle.Primary, startGameButton) {
                    label = "Start"
                }

                leaveButton()
            }
        }
        gameMessage.pin()
        DiscordUnoGame(user, gameMessage, gameThread, translationsProvider)

        respond {
            content = translate("commands.uno.start_game.success")
        }
    }
}
