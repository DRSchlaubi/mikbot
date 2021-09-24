package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.interactions.respond
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.FollowupMessageBehavior
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.create.actionRow
import kotlin.time.Duration

private const val yes = "yes"
private const val no = "no"

class Confirmation(val value: Boolean, private val response: FollowupMessageBehavior) :
    FollowupMessageBehavior by response

/**
 * Initiates a button based confirmation form for [context].
 *
 * @param timeout the [Duration] after which the confirmation process should be aborted (default to false)
 * @param messageBuilder the confirmation message builder
 *
 * @return whether the user confirmed the form or not
 */
suspend fun CommandContext.confirmation(
    context: EphemeralSlashCommandContext<*>,
    timeout: Duration = Duration.seconds(30),
    messageBuilder: MessageBuilder
) = confirmation({ context.respond { it() } }, timeout, messageBuilder)

/**
 * Initiates a button based confirmation form for [context].
 *
 * @param timeout the [Duration] after which the confirmation process should be aborted (default to false)
 * @param messageBuilder the confirmation message builder
 *
 * @return whether the user confirmed the form or not
 */
suspend fun CommandContext.confirmation(
    context: PublicSlashCommandContext<*>,
    timeout: Duration = Duration.seconds(30),
    messageBuilder: MessageBuilder
) = confirmation({ context.respond { it() } }, timeout, messageBuilder)

private suspend fun CommandContext.confirmation(
    sendMessage: suspend (MessageBuilder) -> FollowupMessageBehavior,
    timeout: Duration = Duration.seconds(30),
    messageBuilder: MessageBuilder
): Confirmation {
    val message = sendMessage {
        messageBuilder()

        components {
            actionRow {
                interactionButton(ButtonStyle.Success, yes) {
                    label = translate("general.yes", "general")
                }

                interactionButton(ButtonStyle.Danger, no) {
                    label = translate("general.no", "general")
                }
            }
        }
    }

    val response = message.kord.waitFor<InteractionCreateEvent>(timeout.inWholeMilliseconds) {
        (interaction as? ComponentInteraction)?.let {
            it.message?.id == message.id
        } == true
    } ?: return Confirmation(false, message)

    val interaction = response.interaction as ComponentInteraction
    interaction.acknowledgeEphemeralDeferredMessageUpdate()

    val button = interaction.componentId

    return Confirmation(button == yes, message)
}
