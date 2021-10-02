package dev.schlaubi.musicbot.game.module.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.musicbot.game.leaveButton
import dev.schlaubi.musicbot.game.module.GameModule
import dev.schlaubi.musicbot.module.uno.game.joinGameButton
import dev.schlaubi.musicbot.module.uno.game.startGameButton

/**
 * Adds a /profile command to this [profileCommand].
 *
 * @param gameTitleKey the translation key for the embed title
 * @param threadName the thread name of the game thread
 */
fun GameModule<*, *>.startGameCommand(
    gameTitleKey: String,
    threadName: String
) = publicSubCommand {
    name = "start"
    description = "Starts a new match"

    check {
        // Required for pin()
        requireBotPermissions(Permission.ManageMessages, Permission.ManageThreads)
    }

    action {
        val gameThread = textChannel.startPublicThread(threadName)
        gameThread.addUser(user.id) // Add creator
        val gameMessage = gameThread.createMessage {
            embed {
                title = translate(gameTitleKey)

                footer {
                    text = translateGlobal("game.header.footer")
                }
            }

            actionRow {
                interactionButton(ButtonStyle.Success, joinGameButton) {
                    label = translateGlobal("game.header.join")
                }

                interactionButton(ButtonStyle.Primary, startGameButton) {
                    label = translateGlobal("game.header.start")
                }

                leaveButton(translateGlobal("game.header.leave"))
            }
        }
        gameMessage.pin(reason = "Game Welcome message")
        newGame(user, gameMessage, gameThread, translationsProvider)

        respond {
            content = translateGlobal("game.start.success")
        }
    }
}
