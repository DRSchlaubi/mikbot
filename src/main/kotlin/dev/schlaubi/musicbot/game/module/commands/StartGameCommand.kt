package dev.schlaubi.musicbot.game.module.commands

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.musicbot.game.AbstractGame
import dev.schlaubi.musicbot.game.leaveButton
import dev.schlaubi.musicbot.game.module.GameModule
import dev.schlaubi.musicbot.module.uno.game.joinGameButton
import dev.schlaubi.musicbot.module.uno.game.startGameButton

/**
 * Adds a /profile command to this [profileCommand].
 *
 * @param gameTitleKey the translation key for the embed title
 * @param threadName the thread name of the game thread
 * @param arguments the argument body for this command
 * @param makeNewGame a lambda creating a new game
 */
fun <A : Arguments, G : AbstractGame<*>> GameModule<*, G>.startGameCommand(
    gameTitleKey: String,
    threadName: String,
    arguments: () -> A,
    makeNewGame: suspend PublicSlashCommandContext<A>.(gameMessage: Message, gameThread: ThreadChannelBehavior) -> G?,
    additionalChecks: suspend CheckContext<InteractionCreateEvent>.() -> Unit = {},
    name: String = "start",
    description: String = "Starts a new match"
) = startGameCommand(
    gameTitleKey,
    threadName,
    arguments,
    { },
    { _, gameMessage, gameThread -> makeNewGame(gameMessage, gameThread) },
    additionalChecks,
    name,
    description
)

/**
 * Adds a /profile command to this [profileCommand].
 *
 * @param gameTitleKey the translation key for the embed title
 * @param threadName the thread name of the game thread
 * @param arguments the argument body for this command
 * @param prepareData a callback preparing [Data] before creating the thread
 * @param makeNewGame a lambda creating a new game
 */
fun <A : Arguments, G : AbstractGame<*>, Data> GameModule<*, G>.startGameCommand(
    gameTitleKey: String,
    threadName: String,
    arguments: () -> A,
    prepareData: suspend PublicSlashCommandContext<A>.() -> Data?,
    makeNewGame: suspend PublicSlashCommandContext<A>.(data: Data, gameMessage: Message, gameThread: ThreadChannelBehavior) -> G?,
    additionalChecks: suspend CheckContext<InteractionCreateEvent>.() -> Unit = {},
    name: String = "start",
    description: String = "Starts a new match"
) = publicSubCommand(arguments) {
    this.name = name
    this.description = description

    check {
        // Required for pin()
        requireBotPermissions(Permission.ManageMessages, Permission.ManageThreads)
        additionalChecks()
    }

    action {
        val data = prepareData() ?: return@action
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
        val game = makeNewGame(data, gameMessage, gameThread) ?: return@action
        game.doUpdateWelcomeMessage()
        registerGame(gameThread.id, game)
        respond {
            content = translateGlobal("game.start.success")
        }
    }
}
