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
import kotlin.time.Duration.Companion.seconds

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
    yesWord: String? = null,
    noWord: String? = null,
    timeout: Duration = 30.seconds,
    messageBuilder: MessageBuilder
): Confirmation = confirmation(yesWord, noWord, { respond { it() } }, timeout, messageBuilder)

/**
 * Initiates a button based confirmation form for a [PublicSlashCommandContext].
 *
 * @param timeout the [Duration] after which the confirmation process should be aborted (default to false)
 * @param messageBuilder the confirmation message builder
 *
 * @return whether the user confirmed the form or not
 */
public suspend fun PublicSlashCommandContext<*>.confirmation(
    yesWord: String? = null,
    noWord: String? = null,
    messageBuilder: MessageBuilder,
    timeout: Duration = 30.seconds,
): Confirmation = confirmation(yesWord, noWord, { respond { it() } }, timeout, messageBuilder)

private suspend fun CommandContext.confirmation(
    yesWord: String? = null,
    noWord: String? = null,
    sendMessage: EditableMessageSender,
    timeout: Duration = 30.seconds,
    messageBuilder: MessageBuilder
): Confirmation = confirmation(sendMessage, timeout, messageBuilder, translate = { key, group ->
    translate(key, group)
}, yesWord = yesWord, noWord = noWord)

/**
 * Bare bone confirmation implementation.
 */
public suspend fun confirmation(
    sendMessage: EditableMessageSender,
    timeout: Duration? = 30.seconds,
    messageBuilder: MessageBuilder,
    hasNoOption: Boolean = true,
    translate: Translator,
    yesWord: String? = null,
    noWord: String? = null
): Confirmation {
    val message = sendMessage {
        messageBuilder()

        components {
            actionRow {
                interactionButton(ButtonStyle.Success, yes) {
                    label = yesWord ?: translate("general.yes", "general")
                }

                if (hasNoOption) {
                    interactionButton(ButtonStyle.Danger, no) {
                        label = noWord ?: translate("general.no", "general")
                    }
                }
            }
        }
    }

    val response = message.kord.waitFor<InteractionCreateEvent>(timeout?.inWholeMilliseconds) {
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
