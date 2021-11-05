package dev.schlaubi.mikbot.plugin.api.util

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.FollowupMessageBehavior
import dev.kord.core.behavior.interaction.PublicFollowupMessageBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.create.actionRow
import kotlin.time.Duration

private const val yes = "yes"
private const val no = "no"

@Suppress("DataClassCanBeRecord")
public data class Confirmation(val value: Boolean, private val response: FollowupMessageBehavior) :
    FollowupMessageBehavior by response

/**
 * Initiates a button based confirmation form for a [EphemeralSlashCommandContext].
 *
 * @param timeout the [Duration] after which the confirmation process should be aborted (default to false)
 * @param messageBuilder the confirmation message builder
 *
 * @return whether the user confirmed the form or not
 */
public suspend fun EphemeralSlashCommandContext<*>.confirmation(
    timeout: Duration = Duration.seconds(30),
    messageBuilder: MessageBuilder
): Confirmation = confirmation({ respond { it() } }, timeout, messageBuilder)

/**
 * Initiates a button based confirmation form for a [PublicSlashCommandContext].
 *
 * @param timeout the [Duration] after which the confirmation process should be aborted (default to false)
 * @param messageBuilder the confirmation message builder
 *
 * @return whether the user confirmed the form or not
 */
public suspend fun PublicSlashCommandContext<*>.confirmation(
    timeout: Duration = Duration.seconds(30),
    messageBuilder: MessageBuilder
): Confirmation = confirmation({ respond { it() } }, timeout, messageBuilder)

private suspend fun CommandContext.confirmation(
    sendMessage: EditableMessageSender,
    timeout: Duration = Duration.seconds(30),
    messageBuilder: MessageBuilder
): Confirmation = confirmation(sendMessage, timeout, messageBuilder) { key, group ->
    translate(key, group)
}

/**
 * Bare bone confirmation implementation.
 */
public suspend fun confirmation(
    sendMessage: EditableMessageSender,
    timeout: Duration = Duration.seconds(30),
    messageBuilder: MessageBuilder,
    hasNoOption: Boolean = true,
    translate: Translator
): Confirmation {
    val message = sendMessage {
        messageBuilder()

        components {
            actionRow {
                interactionButton(ButtonStyle.Success, yes) {
                    label = translate("general.yes", "general")
                }

                if (hasNoOption) {
                    interactionButton(ButtonStyle.Danger, no) {
                        label = translate("general.no", "general")
                    }
                }
            }
        }
    }

    val response = message.kord.waitFor<InteractionCreateEvent>(timeout.inWholeMilliseconds) {
        (interaction as? ComponentInteraction)?.let {
            it.message?.id == message.id
        } == true
    } ?: return Confirmation(false, message)

    when (message) {
        is EphemeralFollowupMessage -> message.edit { components = mutableListOf() }
        is PublicFollowupMessageBehavior -> message.edit { components = mutableListOf() }
    }

    val interaction = response.interaction as ComponentInteraction
    interaction.acknowledgeEphemeralDeferredMessageUpdate()

    val button = interaction.componentId

    return Confirmation(button == yes, message)
}
